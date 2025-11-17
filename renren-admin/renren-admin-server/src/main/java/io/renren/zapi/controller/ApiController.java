package io.renren.zapi.controller;


import io.renren.commons.tools.utils.Result;
import io.renren.zapi.merchant.ApiService;
import io.renren.zapi.merchant.dto.*;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("zapi")
public class ApiController {
    @Resource
    private ApiService apiService;

    @GetMapping("health")
    public String health() {
        return "OK";
    }

    /**
     * 充值
     *
     * @param body
     * @param sign
     * @param appKey
     * @return
     */
    @PostMapping("charge")
    public Result<ChargeResponse> charge(@RequestBody String body, @RequestHeader(value = "x-sign", required = false) String sign, @RequestHeader(value = "x-app-key", required = false) String appKey) {
        return apiService.charge(body, sign, appKey);
    }

    /**
     * 提现
     *
     * @param body
     * @param sign
     * @param appKey
     * @return
     */
    @PostMapping("withdraw")
    public Result<WithdrawResponse> withdraw(@RequestBody String body, @RequestHeader(value = "x-sign", required = false) String sign, @RequestHeader(value = "x-app-key", required = false) String appKey) {
        return apiService.withdraw(body, sign, appKey);
    }

    /**
     * 充值查询
     *
     * @param body
     * @param sign
     * @param appKey
     * @return
     */
    @PostMapping("chargeQuery")
    public Result<ChargeQueryResponse> chargeQuery(@RequestBody String body, @RequestHeader(value = "x-sign", required = false) String sign, @RequestHeader(value = "x-app-key", required = false) String appKey) {
        return apiService.chargeQuery(body, sign, appKey);
    }


    /**
     * 提现查询
     *
     * @param body
     * @param sign
     * @param appKey
     * @return
     */
    @PostMapping("withdrawQuery")
    public Result<WithdrawQueryResponse> withdrawQuery(@RequestBody String body, @RequestHeader(value = "x-sign", required = false) String sign, @RequestHeader(value = "x-app-key", required = false) String appKey) {
        return apiService.withdrawQuery(body, sign, appKey);
    }

    /**
     * 余额查询
     *
     * @param body
     * @param sign
     * @param appKey
     * @return
     */
    @PostMapping("balance")
    public Result<BalanceResponse> balance(@RequestBody String body, @RequestHeader(value = "x-sign", required = false) String sign, @RequestHeader(value = "x-app-key", required = false) String appKey) {
        return apiService.balance(body, sign, appKey);
    }

    /**
     * 落地页提交utr
     *
     * @param id
     * @param utr
     * @return
     */
    @PostMapping("submitUtr")
    public Result submitUtr(@RequestParam("id") Long id, @RequestParam("utr") String utr) {
        return apiService.submitUtr(id, utr);
    }

    /**
     * 落地页查询充值状态
     */
    @GetMapping("chargeStatus")
    public Result<Integer> chargeStatus(@RequestParam("id") Long id) {
        int status = apiService.chargeStatus(id);
        Result<Integer> result = new Result<>();
        result.setData(status);
        return result;
    }

    /**
     * 商戶联调自动回调收款交易
     *
     * @param id
     * @param status
     * @return
     */
    @GetMapping("chargeDev")
    public Result chargeDev(@RequestParam("id") long id, @RequestParam("status") int status) {
        return apiService.chargeDev(id, status);
    }

    /**
     * 自动糊掉代付交易
     *
     * @param id
     * @param status
     * @return
     */
    @GetMapping("withdrawDev")
    public Result withdrawDev(@RequestParam("id") Long id, @RequestParam("status") int status) {
        return apiService.withdrawDev(id, status);
    }

    /**
     * 演示环境需要
     */
    @GetMapping("merchantBalance")
    public Result<BigDecimal> merchantBalance(@RequestParam("deptId") Long deptId, @RequestParam("merchantId") Long merchantId) {
        return apiService.merchantInfo(deptId, merchantId);
    }
}
