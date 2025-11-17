package io.renren.zapi.agent;

import cn.hutool.core.codec.Base64;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.renren.dao.SysUserDao;
import io.renren.entity.SysDeptEntity;
import io.renren.entity.SysUserEntity;
import io.renren.zadmin.dao.ZChargeDao;
import io.renren.zadmin.entity.*;
import io.renren.zapi.ledger.AgentLedger;
import io.renren.zapi.channel.dto.ChannelChargeResponse;
import io.renren.zapi.channel.dto.ChannelWithdrawResponse;
import io.renren.zapi.ZConfig;
import io.renren.zapi.ZooConstant;
import io.renren.zapi.event.ChargeTimeoutUserEvent;
import io.renren.zapi.merchant.ApiContext;
import io.renren.zapi.utils.CommonUtils;
import io.renren.zadmin.dao.ZBalanceDao;
import io.renren.zadmin.dao.ZUserCardDao;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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
public class AgentService {

    @Resource
    private TaskScheduler taskScheduler;
    @Resource
    private ZChargeDao zChargeDao;
    @Resource
    private AgentLedger agentLedger;
    @Resource
    private SysUserDao sysUserDao;
    @Resource
    private ZConfig config;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private ZBalanceDao zBalanceDao;
    @Resource
    private ZUserCardDao zUserCardDao;
    @Resource
    private AgentAppService agentAppService;
    @Resource
    private AgentCardStat agentCardStat;
    @Resource
    private TransactionTemplate tx;
    @Resource
    private ApplicationEventPublisher publisher;

    /**
     *  所有代理账户余额， 以及共享账户余额
     * @param chargeEntity
     * @param agents
     * @return
     */
    private HashMap<Long, ZBalanceEntity> getAgentsBalance(ZChargeEntity chargeEntity, Set<Long> agents) {
        HashMap<Long, ZBalanceEntity> agentBalance = new HashMap<>();
        zBalanceDao.selectList(Wrappers.<ZBalanceEntity>lambdaQuery()
                .eq(ZBalanceEntity::getDeptId, chargeEntity.getDeptId())
                .in(ZBalanceEntity::getOwnerId, agents)
                .eq(ZBalanceEntity::getOwnerType, ZooConstant.OWNER_TYPE_AGENT)
                .select(ZBalanceEntity::getId,
                        ZBalanceEntity::getBalance,
                        ZBalanceEntity::getOwnerType,
                        ZBalanceEntity::getOwnerId)
        ).stream().forEach(item -> {
                agentBalance.put(item.getOwnerId(), item);
        });
        return agentBalance;
    }

    /**
     *  所有用户的余额
     * @param chargeEntity
     * @param users
     * @return
     */
    private Map<Long, ZBalanceEntity> getUsersBalance(ZChargeEntity chargeEntity, Set<Long> users) {
        Map<Long, ZBalanceEntity> userBalance = new HashMap<>();
        zBalanceDao.selectList(Wrappers.<ZBalanceEntity>lambdaQuery()
                .eq(ZBalanceEntity::getDeptId, chargeEntity.getDeptId())
                .in(ZBalanceEntity::getOwnerId, users)
                .eq(ZBalanceEntity::getOwnerType, ZooConstant.OWNER_TYPE_USER)
                .select(ZBalanceEntity::getId,
                        ZBalanceEntity::getBalance,
                        ZBalanceEntity::getOwnerType,
                        ZBalanceEntity::getOwnerId)
        ).stream().forEach(item -> {
            userBalance.put(item.getOwnerId(), item);
        });
        return userBalance;
    }

    /**
     * 所有禁用的代理和禁用的用户
     * @return
     */
    private List<Set<Long>> getInvalid() {
        // 禁用用戶 | 代理
        final Set<Long> invalidAgents = new HashSet<>();
        final Set<Long> invalidUsers = new HashSet<>();
        sysUserDao.selectList(Wrappers.<SysUserEntity>lambdaQuery()
                .eq(SysUserEntity::getUserType, ZooConstant.USER_TYPE_ANT)
                .eq(SysUserEntity::getStatus, 0)
        ).stream().forEach(e -> {
            if (e.getUserType().equals(ZooConstant.USER_TYPE_USER)) {
                invalidUsers.add(e.getId());
            } else {
                invalidAgents.add(e.getId());
            }
        });
        return List.of(invalidAgents, invalidUsers);
    }

    /**
     * 充值处理
     * @param chargeEntity
     * @param selected
     * @return
     */
    public ChannelChargeResponse charge(ZChargeEntity chargeEntity, List<ZRouteEntity> selected) {
        List<Long> collected = selected.stream().map(r -> r.getObjectId()).toList();

        // 找出所有在线卡
        List<AgentHashItem> cards;
        if (selected.size() == 0) {
            cards = agentAppService.online(chargeEntity.getDeptId(), 15000L);
        } else {
            cards = agentAppService.online(chargeEntity.getDeptId(), collected, 15000L);
        }
        if (cards.size() == 0) {
            ChannelChargeResponse response = new ChannelChargeResponse();
            response.setError("no agent user online");
            return response;
        }

        // 找出这些卡代理和用户
        final Set<Long> agents = new HashSet<>();
        final Set<Long> users = new HashSet<>();
        cards.stream().forEach(item -> {
            agents.add(item.getAgentId());
            users.add(item.getUserId());
        });

        // 所有卡的成功率情況
        Map<String, Object> successMap = agentCardStat.cardSuccess(chargeEntity.getDeptId());
        Map<String, Object> totalMap = agentCardStat.cardTotal(chargeEntity.getDeptId());

        // 一个时间能分配一次
        String key = "agent:lock:" + chargeEntity.getDeptId();
        synchronized (ZooConstant.agentLocks.intern(key)) {
            // 所有代理的所有余额情况
            Map<Long, ZBalanceEntity> agentBalance = getAgentsBalance(chargeEntity, agents);
            // 所有用户的余额情况
            Map<Long, ZBalanceEntity> userBalance = getUsersBalance(chargeEntity, users);
            // 找出系统中禁用的代理和卡主
            List<Set<Long>> invalid = getInvalid();
            Set<Long> invalidAgents = invalid.get(0);
            Set<Long> invalidUsers = invalid.get(1);

            // 找出合法卡, 补充成功率数据
            List<AgentHashItem> validCards = new ArrayList<>();
            for (AgentHashItem card : cards) {
                Long cardId = card.getCardId();
                Long agentId = card.getAgentId();
                Long userId = card.getUserId();
                if (invalidAgents.contains(agentId)) {
                    continue;
                }
                if (invalidUsers.contains(userId)) {
                    continue;
                }
                BigDecimal ub = userBalance.get(userId).getBalance();
                BigDecimal ab = agentBalance.get(agentId).getBalance();
                if (ub.compareTo(chargeEntity.getAmount()) < 0 || ub.add(ab).compareTo(chargeEntity.getAmount()) < 0) {
                    continue;
                }
                card.setUserBalance(ub);
                card.setAgentBalance(ab);

                // 计算成功率看放入哪个桶中
                Integer success = (Integer) successMap.get(cardId);
                Integer total = (Integer) totalMap.get(cardId);
                if (success == null && total == null) {
                    card.setTotal(1);
                    card.setSuccessRate(0.5);
                }
                card.setSuccessRate(success/total);
                card.setTotal(total);
                validCards.add(card);
            }

            // 按接单笔数排序
            List<AgentHashItem> sortedByTotal = validCards.stream()
                    .sorted(Comparator.comparingInt(AgentHashItem::getTotal))
                    .collect(Collectors.toList());
            int midIndex = sortedByTotal.size() / 2;
            if (sortedByTotal.size() % 2 == 1) {
                midIndex += 1;
            }

            // 取前50%按成功率倒序
            List<AgentHashItem> sortedBySuccessRate = sortedByTotal.subList(0, midIndex)
                    .stream()
                    .sorted(Comparator.comparingDouble(AgentHashItem::getSuccessRate).reversed())
                    .collect(Collectors.toList());

            // 再取前50%
            midIndex = sortedBySuccessRate.size() / 2;
            if (sortedByTotal.size() % 2 == 1) {
                midIndex += 1;
            }
            List<AgentHashItem> finalCards = sortedBySuccessRate.subList(0, midIndex);
            int fIndex = new Random().nextInt(finalCards.size());

            // 随机
            AgentHashItem item = finalCards.get(fIndex);
            log.info("选择接单卡:{}", item);

            //
            return serveCharge(chargeEntity, item);
        }
    }

    private ChannelChargeResponse serveCharge(ZChargeEntity chargeEntity, AgentHashItem item) {
        ZUserCardEntity card = zUserCardDao.selectById(item.getCardId());
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
        Long tn = agentCardStat.increaseCardTn(card.getDeptId(), card.getId());
        String tnstr = CommonUtils.base36(tn);
        map.put("tn", tnstr);

        String json = null;
        try {
            json = objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String encode = Base64.encode(json);
        String payUrl = config.getCdnUrl() + "/#/" + dept.getCurrency() + "?mode=agent&base64=" + encode;

        // 依据不同的要素生成payUrl
        ChannelChargeResponse response = new ChannelChargeResponse();
        response.setUpi(card.getAccountUpi());
        response.setPayUrl(payUrl);

        chargeEntity.setAgentId(item.agentId);
        chargeEntity.setUserId(item.userId);
        chargeEntity.setUserCardId(card.getId());
        chargeEntity.setUserCardUser(card.getAccountUser());
        chargeEntity.setUserCardNo(card.getAccountNo());

        SysUserEntity user = sysUserDao.selectById(item.getUserId());
        SysUserEntity agent = sysUserDao.selectById(item.getAgentId());

        // 记账
        tx.executeWithoutResult(status -> {
            zChargeDao.update(null, Wrappers.<ZChargeEntity>lambdaUpdate()
                    .eq(ZChargeEntity::getId, chargeEntity.getId())
                    .set(ZChargeEntity::getProcessStatus, ZooConstant.CHARGE_STATUS_PROCESSING)
                    .set(ZChargeEntity::getUserCardId, item.getCardId())
                    .set(ZChargeEntity::getUsername, user.getUsername())
                    .set(ZChargeEntity::getUserCardUser, card.getAccountUser())
                    .set(ZChargeEntity::getUserCardNo, card.getAccountNo())
                    .set(ZChargeEntity::getUserRate, user.getUserRate())
                    .set(ZChargeEntity::getAgentId, agent.getId())
                    .set(ZChargeEntity::getAgentName, agent.getUsername())
                    .set(ZChargeEntity::getAgentRate, agent.getAgentName())
            );
            agentLedger.userCollectAssigned(chargeEntity);
        });

        // 延迟任务
        taskScheduler.schedule(() -> {
            publisher.publishEvent(new ChargeTimeoutUserEvent(null, chargeEntity.getId()));
        }, Instant.now().plusSeconds(6 * 60));

        // 增加接单数值
        agentCardStat.increaseCardTotal(chargeEntity.getDeptId(), card.getId());

        return response;
    }

    // 找个某个代理下的卡主付
    public ChannelWithdrawResponse withdraw(ZWithdrawEntity withdrawEntity, Long selectedAgentId) {
        return null;
    }
}
