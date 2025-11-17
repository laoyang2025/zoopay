package io.renren.zapi.ant;

import cn.hutool.core.codec.Base64;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.renren.dao.SysDeptDao;
import io.renren.dao.SysUserDao;
import io.renren.entity.SysDeptEntity;
import io.renren.entity.SysUserEntity;
import io.renren.zadmin.dao.ZChargeDao;
import io.renren.zapi.ZConfig;
import io.renren.zapi.ledger.AntLedger;
import io.renren.zapi.ZooConstant;
import io.renren.zapi.channel.dto.ChannelChargeResponse;
import io.renren.zapi.channel.dto.ChannelWithdrawResponse;
import io.renren.zapi.event.ChargeTimeoutAntEvent;
import io.renren.zapi.merchant.ApiContext;
import io.renren.zadmin.dao.ZAntCardDao;
import io.renren.zadmin.dao.ZBalanceDao;
import io.renren.zadmin.entity.ZAntCardEntity;
import io.renren.zadmin.entity.ZBalanceEntity;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zadmin.entity.ZWithdrawEntity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AntService {
    @Resource
    private ZConfig config;
    @Resource
    private SysDeptDao sysDeptDao;
    @Resource
    private ZChargeDao zChargeDao;
    @Resource
    private ZBalanceDao zBalanceDao;
    @Resource
    private SysUserDao sysUserDao;
    @Resource
    private ZAntCardDao zAntCardDao;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private AntAppService antAppService;
    @Resource
    private AntCardStat antCardStat;
    @Resource
    private TransactionTemplate tx;
    @Resource
    private ApplicationEventPublisher publisher;
    @Resource
    private AntLedger antLedger;
    @Resource
    private TaskScheduler taskScheduler;

    /**
     * 充值派单
     * @param chargeEntity
     * @return
     */
    public ChannelChargeResponse charge(ZChargeEntity chargeEntity) {
        // 找出所有在线用户
        List<AntHashItem> cards;
        cards = antAppService.online(chargeEntity.getDeptId(), 15000L);
        if (cards.size() == 0) {
            ChannelChargeResponse response = new ChannelChargeResponse();
            response.setError("no ant user online");
            return response;
        }

        // 所有卡的成功率情況
        Map<String, Object> successMap = antCardStat.cardSuccess(chargeEntity.getDeptId());
        Map<String, Object> totalMap = antCardStat.cardTotal(chargeEntity.getDeptId());

        // 一个时间能分配一次
        String key = "ant:lock:" + chargeEntity.getDeptId();
        synchronized (ZooConstant.antLocks.intern(key)) {
            // 所有用户的余额情况
            Map<Long, ZBalanceEntity> antsBalance = getAntsBalance(chargeEntity);

            // 找出系统中禁用的代理和卡主
            Set<Long> invalid = getInvalid();

            // 找出合法卡, 补充成功率数据
            List<AntHashItem> validCards = new ArrayList<>();
            for (AntHashItem card : cards) {
                Long cardId = card.getCardId();
                Long userId = card.getUserId();
                if (invalid.contains(userId)) {
                    continue;
                }
                BigDecimal balance = antsBalance.get(userId).getBalance();
                if (balance.compareTo(chargeEntity.getAmount()) < 0) {
                    continue;
                }
                card.setUserBalance(balance);

                // 计算成功率看放入哪个桶中
                Integer success = (Integer) successMap.get(cardId);
                Integer total = (Integer) totalMap.get(cardId);
                if (success == null && total == null) {
                    card.setTotal(1);
                    card.setSuccessRate(0.5);
                }
                card.setSuccessRate(success / total);
                card.setTotal(total);
                validCards.add(card);
            }

            // 按接单笔数排序
            List<AntHashItem> sortedByTotal = validCards.stream()
                    .sorted(Comparator.comparingInt(AntHashItem::getTotal))
                    .collect(Collectors.toList());
            int midIndex = sortedByTotal.size() / 2;
            if (sortedByTotal.size() % 2 == 1) {
                midIndex += 1;
            }

            // 取前50%按成功率倒序
            List<AntHashItem> sortedBySuccessRate = sortedByTotal.subList(0, midIndex)
                    .stream()
                    .sorted(Comparator.comparingDouble(AntHashItem::getSuccessRate).reversed())
                    .collect(Collectors.toList());

            // 再取前50%
            midIndex = sortedBySuccessRate.size() / 2;
            if (sortedByTotal.size() % 2 == 1) {
                midIndex += 1;
            }
            List<AntHashItem> finalCards = sortedBySuccessRate.subList(0, midIndex);
            int fIndex = new Random().nextInt(finalCards.size());

            // 随机
            AntHashItem item = finalCards.get(fIndex);


            return serveCharge(chargeEntity, item);
        }
    }

    @NotNull
    private ChannelChargeResponse serveCharge(ZChargeEntity chargeEntity, AntHashItem item) {
        ZAntCardEntity card = zAntCardDao.selectById(item.getCardId());
        ApiContext context = ApiContext.getContext();
        SysDeptEntity dept = context.getDept();
        SysUserEntity merchant = context.getMerchant();
        Map<String, Object> map = new HashMap<>();
        map.put("dev", merchant.getDev());
        map.put("accountNo",   card.getAccountNo());
        map.put("accountUser", card.getAccountUser());
        map.put("accountBank", card.getAccountBank());
        map.put("accountIfsc", card.getAccountIfsc());
        map.put("accountUpi",  card.getAccountUpi());
        map.put("accountInfo", card.getAccountInfo());
        map.put("amount", chargeEntity.getAmount());
        map.put("id", chargeEntity.getId().toString());
        map.put("deadline", new Date().getTime() + 300 * 1000);

        String json = null;
        try {
            json = objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String encode = Base64.encode(json);

        String payUrl = config.getCdnUrl() + "/#/" + dept.getCurrency() + "?mode=ant&base64=" + encode;

        // 依据不同的要素生成payUrl
        ChannelChargeResponse response = new ChannelChargeResponse();
        response.setUpi(card.getAccountUpi());
        response.setPayUrl(payUrl);

        // 记账
        SysUserEntity ant = sysUserDao.selectById(item.getUserId());
        tx.executeWithoutResult(status -> {
            zChargeDao.update(null, Wrappers.<ZChargeEntity>lambdaUpdate()
                    .eq(ZChargeEntity::getId, chargeEntity.getId())
                    .set(ZChargeEntity::getProcessStatus, ZooConstant.CHARGE_STATUS_PROCESSING)
                    .set(ZChargeEntity::getAntCardId, item.getCardId())
                    .set(ZChargeEntity::getAntName, ant.getUsername())
                    .set(ZChargeEntity::getAntId, item.getUserId())
            );
            antLedger.antCollectAssigned(chargeEntity);
        });

        // 延迟任务
        taskScheduler.schedule(() -> {
            publisher.publishEvent(new ChargeTimeoutAntEvent(null, chargeEntity.getId()));
        }, Instant.now().plusSeconds(6 * 60));

        antCardStat.increaseCardTotal(chargeEntity.getDeptId(), card.getId());

        return response;
    }

    /**
     * 代付
     * @param withdrawEntity
     * @return
     */
    public ChannelWithdrawResponse withdraw(ZWithdrawEntity withdrawEntity) {
        return null;
    }


    /**
     * 所有码农余额
     * @param chargeEntity
     * @return
     */
    private Map<Long, ZBalanceEntity> getAntsBalance(ZChargeEntity chargeEntity) {
        Map<Long, ZBalanceEntity> ants = new HashMap<>();
        zBalanceDao.selectList(Wrappers.<ZBalanceEntity>lambdaQuery()
                .eq(ZBalanceEntity::getDeptId, chargeEntity.getDeptId())
                .eq(ZBalanceEntity::getOwnerType, ZooConstant.OWNER_TYPE_ANT)
                .select(ZBalanceEntity::getId,
                        ZBalanceEntity::getBalance,
                        ZBalanceEntity::getOwnerType,
                        ZBalanceEntity::getOwnerId)
        ).stream().forEach(item -> {
            ants.put(item.getOwnerId(), item);
        });
        return ants;
    }

    /**
     * 禁用用户
     * @return
     */
    private Set<Long> getInvalid() {
        // 禁用用戶 | 代理
        final Set<Long> invalidUsers = new HashSet<>();
        sysUserDao.selectList(Wrappers.<SysUserEntity>lambdaQuery()
                .in(SysUserEntity::getUserType, List.of(ZooConstant.USER_TYPE_USER, ZooConstant.USER_TYPE_AGENT))
                .eq(SysUserEntity::getStatus, 0)
        ).stream().forEach(e -> {
                invalidUsers.add(e.getId());
        });
        return invalidUsers;
    }

}
