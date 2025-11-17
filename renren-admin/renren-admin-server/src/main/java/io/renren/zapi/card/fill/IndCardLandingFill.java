package io.renren.zapi.card.fill;

import io.renren.entity.SysDeptEntity;
import io.renren.entity.SysUserEntity;
import io.renren.zadmin.dto.ZCardLogDTO;
import io.renren.zadmin.entity.ZCardEntity;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zapi.ZooConstant;
import io.renren.zapi.card.CardAppService;
import io.renren.zapi.utils.CommonUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;


// 印度币种的落地页数据填充
@Service
@Slf4j
public class IndCardLandingFill implements CardLandingFill {
    @Resource
    private TaskScheduler taskScheduler;
    @Resource
    private CardAppService cardAppService;

    @Override
    public void fill(Map<String, Object> map, ZCardEntity zCardEntity, ZChargeEntity chargeEntity, SysDeptEntity deptEntity, SysUserEntity merchant) {
        if (chargeEntity.getPayCode().equals(ZooConstant.IND_PAYCODE_UPI) ) {
            this.upiFill(map, zCardEntity, chargeEntity, deptEntity, merchant);
            return;
        }
        log.warn("payCode not matched: {}", chargeEntity);
    }

    /**
     * upi产品
     * @param map
     * @param zCardEntity
     */
    private void upiFill(Map<String, Object> map, ZCardEntity zCardEntity, ZChargeEntity chargeEntity, SysDeptEntity deptEntity, SysUserEntity merchant) {
        map.put("accountUpi", zCardEntity.getAccountUpi());
        map.put("accountInfo", zCardEntity.getAccountInfo());
        map.put("submitUtr", true);  // 需要提交utr
        map.put("showQrcode", true); // 需要展示qrcode
        if (merchant.getDev() == 1) {
            log.info("merchant dev = {}", merchant.getDev());
            String fakeUtr =  null;
            fakeUtr = "4" + CommonUtils.randomDigitString(11);
            map.put("dev", merchant.getDev());
            map.put("utr", fakeUtr);  // 提示用户输入这个UTR

            // 基础的fake
            ZCardLogDTO fakeOne = fakeLog(zCardEntity, chargeEntity, deptEntity);

            // upi相关的fake
            fakeOne.setUtr(fakeUtr);
            fakeOne.setTn(null);
            fakeOne.setNarration("UPI/4" +CommonUtils.randomDigitString(11) + "/1231231/payerIJs/asdf");

            // 上报
            this.fakeReport(fakeOne);
        }
    }


    /**
     * 卡卡产品
     * @param map
     * @param zCardEntity
     */
    private void cardFill(Map<String, Object> map, ZCardEntity zCardEntity, ZChargeEntity chargeEntity, SysDeptEntity deptEntity, SysUserEntity merchant) {
        map.put("accountUpi", zCardEntity.getAccountUpi());
        map.put("accountUser", zCardEntity.getAccountUser());
        map.put("accountNo", zCardEntity.getAccountNo());
        map.put("accountBank", zCardEntity.getAccountBank());
        if (merchant.getDev() == 1) {
            String fakeUtr = "4" + CommonUtils.randomDigitString(11);
            map.put("dev", merchant.getDev());
            map.put("utr", fakeUtr);  // 提示用户输入这个UTR
            ZCardLogDTO fakeLog = fakeLog(zCardEntity, chargeEntity, deptEntity);
            fakeReport(fakeLog);
        }
    }

    private ZCardLogDTO fakeLog(ZCardEntity zCardEntity, ZChargeEntity chargeEntity, SysDeptEntity deptEntity) {
        BigDecimal fakeBalance = null;
        ZCardLogDTO fakeOne = new ZCardLogDTO();
        fakeOne.setCardId(zCardEntity.getId());
        fakeOne.setCardNo(zCardEntity.getAccountNo());
        fakeOne.setCardUser(zCardEntity.getAccountUser());
        fakeOne.setAmount(chargeEntity.getAmount());
        fakeOne.setDeptId(deptEntity.getId());
        fakeOne.setDeptName(deptEntity.getName());
        fakeOne.setBalance(BigDecimal.TEN);
        fakeOne.setFlag("plus");
        return fakeOne;
    }

    public void fakeReport(ZCardLogDTO fakeOne) {
        // 模拟上报
        List<ZCardLogDTO> fakeLogs = List.of(fakeOne);
        taskScheduler.schedule(() -> {
            log.info("测试商户, 模拟上报流水");
            cardAppService.report(fakeOne.getDeptId(), fakeOne.getDeptName(), fakeOne.getCardId(), fakeLogs, fakeOne.getBalance());
        }, Instant.now().plusSeconds(5));
    }
}
