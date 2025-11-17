package io.renren.zsocket;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.renren.commons.tools.redis.RedisUtils;
import io.renren.commons.tools.utils.Result;
import io.renren.entity.SysDeptEntity;
import io.renren.zadmin.dao.ZCardDao;
import io.renren.zadmin.dao.ZChargeDao;
import io.renren.zadmin.dao.ZSmsDao;
import io.renren.zadmin.dto.ZCardDTO;
import io.renren.zadmin.entity.ZCardEntity;
import io.renren.zadmin.entity.ZCardLogEntity;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zadmin.entity.ZSmsEntity;
import io.renren.zadmin.service.ZCardService;
import io.renren.zapi.AlarmService;
import io.renren.zapi.UmiOcrService;
import io.renren.zapi.ZConfig;
import io.renren.zapi.ZooConstant;
import io.renren.zapi.event.ChargeSuccessEvent;
import io.renren.zapi.ledger.ZLedger;
import io.renren.zapi.route.RouteService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class SocketMessageService {
    @Resource
    private ApplicationEventPublisher publisher;
    @Resource
    private TransactionTemplate tx;
    @Resource
    private ZLedger ledger;
    @Resource
    private RouteService routeService;
    @Resource
    private AlarmService alarmService;
    @Resource
    private ZSmsDao zSmsDao;
    @Resource
    private ZChargeDao zChargeDao;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private ZConfig zConfig;
    @Resource
    private UmiOcrService umiOcrService;
    @Resource
    private ZCardDao zCardDao;

    public String adminCaptchaKey(Long deptId, Long timeout, String image) {
        SysDeptEntity dept = routeService.getDept(deptId);
        String mykey = UUID.randomUUID().toString();

        // 先清空队列
        while (true) {
            if (redisUtils.rightPopNoWait(mykey) == null) {
                break;
            }
        }
        String prompt = "请输入验证码";

        // 通知管理后台去输入验证码
        final Long finalDeptId = deptId;
        CompletableFuture.runAsync(() -> {
            Map<String, Object> map = new HashMap<>();
            map.put("callback", dept.getApiDomain() + "/sys/zms/msg/callback/captcha/" + mykey);
            map.put("image", image);
            map.put("timeout", timeout);
            log.debug("[{}] - admin captcha websocket push, and wait on key[{}]", deptId, mykey);
            SocketAdmin.sendMessage(finalDeptId, ZooConstant.MSG_TYPE_CAPTCHA, prompt, map);
        });

        return mykey;

    }


    /**
     * 手动采集短信验证码
     *
     * @param deptId
     * @param phone
     * @param timeout
     * @param prompt
     * @return
     */
    public String adminSmsKey(Long deptId, String phone, Long timeout, String prompt) {
        SysDeptEntity dept = routeService.getDept(deptId);
        String mykey = UUID.randomUUID().toString();
        Map<String, Object> map = new HashMap<>();
        map.put("callback", dept.getApiDomain() + "/sys/zms/msg/callback/sms/" + mykey);
        map.put("phone", phone);
        map.put("timeout", timeout);

        // 先清空队列
        while (true) {
            if (redisUtils.rightPopNoWait(phone) == null) {
                break;
            }
        }
        if (prompt == null) {
            prompt = "你有新的短信输入任务，请及时处理";
        }

        // 通知管理后台去输入验证码
        String finalPrompt = prompt;
        CompletableFuture.runAsync(() -> {
            SocketAdmin.sendMessage(deptId, ZooConstant.MSG_TYPE_SMS, finalPrompt, map);
        });

        return mykey;
    }

    public Result<String> getMessageByKey(String key, long timeout) {
        // wait to get msg
        var rtn = new Result<String>();
        try {
            Object str = redisUtils.rightPop(key, timeout);
            String code = (String) str;
            rtn.setData(code);
            return rtn;
        } catch (Exception ex) {
            return Result.fail(9999, "timeout");
        }
    }


    public void setOtp(ZSmsEntity zSmsEntity, List<ZCardDTO> cardList) {
        // 匹配各银行短信内容
        List<Pattern> smsPatterns = List.of(
                Pattern.compile("Dear Customer.* OTP for Reference .* is (\\d{6}). Please use.*"),  // mahara
                Pattern.compile("(\\d{8}) is OTP to authenticate login credential.*SBI\\.")  // sbi
        );
        String phone = zSmsEntity.getPhone();
        if (phone == null) {
            // 从card里用deviceId匹配获取卡的手机号
            // todo:
        }
        zSmsEntity.setPhone(phone);

        // 找到OTP, 就往消息队列里放
        for (Pattern smsPattern : smsPatterns) {
            Matcher matcher = smsPattern.matcher(zSmsEntity.getContent());
            if (matcher.find()) {
                String otp = matcher.group(1);
                redisUtils.leftPush(phone, otp);
                break;
            }
        }
    }


    // todo: utr + amount 匹配与设置
    public void setUtr(ZSmsEntity zSmsEntity) {
    }

    // 查找收款流水
    public ZChargeEntity utrMatch(ZSmsEntity entity) {
        // utr到收款流水里匹配
        List<ZChargeEntity> zChargeEntities = zChargeDao.selectList(Wrappers.<ZChargeEntity>lambdaQuery()
                .eq(ZChargeEntity::getCardId, entity.getCardId())
                .eq(ZChargeEntity::getUtr, entity.getUtr())
                .eq(ZChargeEntity::getHandleMode, ZooConstant.PROCESS_MODE_CARD)
        );
        // 无收款流水匹配
        if (zChargeEntities.size() == 0) {
            return null;
        }
        //
        if (zChargeEntities.size() == 1) {
            ZChargeEntity found = zChargeEntities.get(0);
            if (found.getProcessStatus().equals(ZooConstant.CHARGE_STATUS_PROCESSING)) {
                return found;
            } else if (found.getProcessStatus().equals(ZooConstant.CHARGE_STATUS_SUCCESS)) {
                zSmsDao.update(null, Wrappers.<ZSmsEntity>lambdaUpdate()
                        .set(ZSmsEntity::getMatchStatus, ZooConstant.MATCH_SUCCESS)
                        .eq(ZSmsEntity::getId, entity.getId()));
                entity.setFailCount(100);
                return null;
            }
        } else {
            String msg = String.format("UTR[%s]匹配多笔收款[%d]", entity.getUtr(), zChargeEntities.size());
            log.warn(msg);
            alarmService.warn(entity.getDeptId(), "匹配异常", msg);
            entity.setFailCount(16);
            return null;
        }
        return null;
    }

    // 执行匹配
    public void matchCollect(ZSmsEntity entity) {
        ZChargeEntity matched = null;
        matched = utrMatch(entity);
        // 没有匹配上
        if (matched == null) {
            int fcnt = entity.getFailCount() == null ? 0 : entity.getFailCount();
            if (fcnt == 100) {
                return;
            }
            if (fcnt > 16) {
                fcnt = 16;
            }
            zSmsDao.update(null, Wrappers.<ZSmsEntity>lambdaUpdate()
                    .eq(ZSmsEntity::getId, entity.getId())
                    .set(ZSmsEntity::getFailCount, fcnt + 1)
                    .set(ZSmsEntity::getMatchStatus, ZooConstant.MATCH_FAIL)
            );
            return;
        }

        if (!matched.getAmount().subtract(entity.getAmount()).setScale(0).equals(BigDecimal.ZERO)) {
            String msg = String.format("金额不匹配, utr:%s, 收款金额:%s, 银行金额:%s", entity.getUtr(), matched.getAmount(), entity.getAmount());
            zSmsDao.update(null, Wrappers.<ZSmsEntity>lambdaUpdate()
                    .eq(ZSmsEntity::getId, entity.getId())
                    .set(ZSmsEntity::getFailCount, 16)
                    .set(ZSmsEntity::getMatchStatus, ZooConstant.MATCH_FAIL)
            );
            alarmService.warn(entity.getDeptId(), "匹配异常", msg);
            return;
        }

        // 匹配成功
        matchSuccess(matched, entity);
    }

    /**
     * 短信与收款流水匹配
     *
     * @param finalMatched
     * @param zSmsEntity
     */
    private void matchSuccess(ZChargeEntity finalMatched, ZSmsEntity zSmsEntity) {
        // 锁住商户 + 事务
        synchronized (ZooConstant.getMerchantLock(finalMatched.getMerchantId())) {
            tx.executeWithoutResult(status -> {
                // 3. 商户余额记账
                ledger.merchantChargeSuccess(finalMatched);

                // 1. 更新状态
                int update = zChargeDao.update(Wrappers.<ZChargeEntity>lambdaUpdate()
                        .eq(ZChargeEntity::getId, finalMatched.getId())
                        .eq(ZChargeEntity::getProcessStatus, ZooConstant.CHARGE_STATUS_PROCESSING)
                        .set(ZChargeEntity::getProcessStatus, ZooConstant.CHARGE_STATUS_SUCCESS)
                        .set(ZChargeEntity::getMerchantFee, finalMatched.getMerchantFee())
                        .set(ZChargeEntity::getMerchantPrincipal, finalMatched.getMerchantPrincipal())
                );
                if (update != 1) {
                    status.setRollbackOnly();
                    return;
                }

                // 2. 更新銀行流水
                update = zSmsDao.update(null, Wrappers.<ZSmsEntity>lambdaUpdate()
                        .eq(ZSmsEntity::getId, zSmsEntity.getId())
                        .set(ZSmsEntity::getMatchStatus, ZooConstant.MATCH_SUCCESS)
                        .set(ZSmsEntity::getChargeId, finalMatched.getId())
                );
                if (update != 1) {
                    status.setRollbackOnly();
                    return;
                }
            });
        }
        // 通知商户
        publisher.publishEvent(new ChargeSuccessEvent(this, finalMatched.getId()));
    }


    /**
     * 定时任务调用
     */
    public void matchTask() {
        // 扫描3小时之前到现在的流水(收款流水 + 状态是待匹配|匹配失败 + 失败次数<16)
        Date date = DateUtils.addHours(new Date(), -3);
        List<ZSmsEntity> zSmsEntities = zSmsDao.selectList(Wrappers.<ZSmsEntity>lambdaQuery()
                .in(ZSmsEntity::getMatchStatus, List.of(ZooConstant.MATCH_TODO, ZooConstant.MATCH_FAIL))
                .gt(ZSmsEntity::getCreateDate, date)
                .lt(ZSmsEntity::getFailCount, 16)
        );
        if (zSmsEntities.size() == 0) {
            return;
        }
        log.debug("match z_sms: {}", zSmsEntities.size());

        for (ZSmsEntity entity : zSmsEntities) {
            matchCollect(entity);
        }
    }

    /**
     * @param deptId
     * @param fileId
     */
    public void reportPictureKey(Long deptId, String fileId, String phone) {

        // 图片识别
        Path filePath = Paths.get(zConfig.getUploadDir(), fileId);
        String imageFile = filePath.toString();
        String[] text = umiOcrService.getText(imageFile);

        // 查询手机对应的卡
        ZCardEntity zCardEntity = zCardDao.selectOne(Wrappers.<ZCardEntity>lambdaQuery()
                .eq(ZCardEntity::getPhone, phone)
                .eq(ZCardEntity::getDeptId, deptId)
        );
        String cardCode = zCardEntity.getCardCode();

        // 依据不同的卡来识别totp
        String otp = null;
        String content = "";
        if (cardCode.equals("kvb")) {
            // KVB 060988 Secured byRSA
            // todo parse the otp
            otp = "060988";
        }
        if (otp == null) {
            return;
        }

        // 入库totp
        ZSmsEntity zSmsEntity = new ZSmsEntity();
        zSmsEntity.setContent(content);
        zSmsEntity.setPhone(phone);
        zSmsEntity.setDeptId(deptId);
        zSmsEntity.setMd5(DigestUtil.md5Hex(content));
        zSmsDao.insert(zSmsEntity);

        // 推送消息到队列
        redisUtils.leftPush(phone, otp);
    }
}


