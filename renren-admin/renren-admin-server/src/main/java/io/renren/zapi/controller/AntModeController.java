package io.renren.zapi.controller;


import io.renren.commons.security.user.MyUserDetail;
import io.renren.commons.security.user.SecurityUser;
import io.renren.commons.tools.exception.RenException;
import io.renren.commons.tools.utils.Result;
import io.renren.zapi.ZooConstant;
import io.renren.zapi.ant.AntAppService;
import io.renren.zadmin.dto.ZAntLogDTO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("zant")
public class AntModeController {

    @Resource
    private AntAppService antAppService;

    /**
     * 心跳上报
     */
    @GetMapping("heartbeat")
    public Result heartbeat(@RequestParam("cardId") Long cardId) {
        MyUserDetail user = SecurityUser.getUser();
        if(user.getUserType().equals(ZooConstant.USER_TYPE_USER)) {
            throw new RenException("invalid user");
        }
        antAppService.heartbeat(user, cardId);
        return new Result();
    }

    /**
     * 上报银行流水
     */
    @PostMapping("report")
    public Result report(@RequestParam("cardId") Long cardId, @RequestParam(value = "balance", required = false) BigDecimal balance,  @RequestBody List<ZAntLogDTO> logs) {
        MyUserDetail user = SecurityUser.getUser();
        antAppService.report(user, cardId, logs, balance);
        return new Result();
    }


    /**
     * 抢代付单
     * @param id
     * @return
     */
    @GetMapping("claim")
    public Result<ClaimResponse> claim(@RequestParam("id") Long id) {
        return antAppService.claimWithdraw(id);
    }


}
