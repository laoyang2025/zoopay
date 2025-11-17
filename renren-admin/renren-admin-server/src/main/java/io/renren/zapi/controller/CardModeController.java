package io.renren.zapi.controller;


import io.renren.commons.security.user.MyUserDetail;
import io.renren.commons.security.user.SecurityUser;
import io.renren.commons.tools.utils.Result;
import io.renren.zapi.card.CardAppService;
import io.renren.zadmin.dto.ZCardLogDTO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("zcard")
public class CardModeController {

    @Resource
    private CardAppService cardAppService;

    /**
     * 上报银行流水
     */
    @PostMapping("report")
    public Result report(@RequestParam("cardId") Long cardId,
                         @RequestParam("deptId") Long deptId,
                         @RequestParam("deptName") String deptName,
                         @RequestParam(value = "balance", required = false) BigDecimal balance,
                         @RequestBody List<ZCardLogDTO> logs) {
        cardAppService.report(deptId, deptName, cardId, logs, balance);
        return new Result();
    }

}
