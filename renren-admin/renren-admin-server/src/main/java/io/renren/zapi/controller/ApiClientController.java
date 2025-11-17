package io.renren.zapi.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import io.renren.entity.SysUserEntity;
import io.renren.zapi.ZConfig;
import io.renren.zapi.merchant.ApiClient;
import io.renren.zapi.merchant.dto.*;
import io.renren.zapi.route.RouteService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 在线测试用的客户端
 */
@RestController
@RequestMapping("zclient")
@Slf4j
public class ApiClientController {

    @Resource
    private ZConfig config;

    private ApiClient demoClient;

    @PostConstruct
    public void init() {
        this.demoClient = new ApiClient("http://127.0.0.1:" + this.port);
    }

    @Resource
    private RouteService routeService;
    @Value("${server.port}")
    private String port;

    @GetMapping("charge")
    public Result<ChargeResponse> charge(
            @RequestParam("deptId") Long deptId,
            @RequestParam("merchantId") Long merchantId,
            @RequestParam(value = "payCode", required = false) String payCode,
            @RequestParam("amount") BigDecimal amount
    ) throws JsonProcessingException {
        ChargeRequest chargeRequest = new ChargeRequest();
        SysUserEntity merchant = routeService.getSysUser(merchantId);
        String appKey = deptId + "-" + merchantId;
        String secretKey = merchant.getSecretKey();
        chargeRequest.setAmount(amount);
        chargeRequest.setPayCode(payCode);
        chargeRequest.setOrderId(String.valueOf(new Date().getTime()));
        chargeRequest.setNotifyUrl("http://localhost:" + port + "/sys/zclient/chargeNotify");
        chargeRequest.setCallbackUrl("http://localhost:" + port + "/sys/zclient/chargeNotify");
        chargeRequest.setMemo("NA");
        return demoClient.charge(chargeRequest, appKey, secretKey);
    }

    @GetMapping("withdraw")
    public Result<WithdrawResponse> withdraw(@RequestParam("deptId") Long deptId,
                                             @RequestParam("merchantId") Long merchantId,
                                             @RequestParam("amount") BigDecimal amount,
                                             @RequestParam("accountUser") String accountUser,
                                             @RequestParam("accountNo") String accountNo,
                                             @RequestParam("accountIfsc") String accountIfsc
    ) throws JsonProcessingException {
        WithdrawRequest withdrawRequest = new WithdrawRequest();
        SysUserEntity merchant = routeService.getSysUser(merchantId);
        String appKey = deptId + "-" + merchantId;
        String secretKey = merchant.getSecretKey();
        withdrawRequest.setAmount(amount);
        withdrawRequest.setOrderId(String.valueOf(new Date().getTime()));
        withdrawRequest.setNotifyUrl("http://localhost:" + port + "/sys/zclient/withdrawNotify");
        withdrawRequest.setCallbackUrl("http://localhost:" + port + "/sys/zclient/withdrawNotify");
        withdrawRequest.setMemo("NA");
        withdrawRequest.setAccountBank("bank");
        withdrawRequest.setAccountNo(accountNo);
        withdrawRequest.setAccountUser(accountUser);
        withdrawRequest.setAccountIfsc(accountIfsc);
        return demoClient.withdraw(withdrawRequest, appKey, secretKey);
    }

    @GetMapping("chargeQuery")
    public Result<ChargeQueryResponse> chargeQuery(@RequestParam("deptId") Long deptId, @RequestParam("merchantId") Long merchantId, @RequestParam("orderId") String orderId) throws JsonProcessingException {
        ChargeQueryRequest chargeQueryRequest = new ChargeQueryRequest();
        chargeQueryRequest.setOrderId(orderId);
        SysUserEntity merchant = routeService.getSysUser(merchantId);
        String appKey = deptId + "-" + merchantId;
        String secretKey = merchant.getSecretKey();
        return demoClient.chargeQuery(chargeQueryRequest, appKey, secretKey);
    }

    @GetMapping("withdrawQuery")
    public Result<WithdrawQueryResponse> withdrawQuery(@RequestParam("deptId") Long deptId, @RequestParam("merchantId") Long merchantId, @RequestParam("orderId") String orderId) throws JsonProcessingException {
        WithdrawQueryRequest withdrawQueryRequest = new WithdrawQueryRequest();
        withdrawQueryRequest.setOrderId(orderId);
        SysUserEntity merchant = routeService.getSysUser(merchantId);
        String appKey = deptId + "-" + merchantId;
        String secretKey = merchant.getSecretKey();
        return demoClient.withdrawQuery(withdrawQueryRequest, appKey, secretKey);
    }

    @GetMapping("balance")
    public Result<BalanceResponse> balance(@RequestParam("deptId") Long deptId, @RequestParam("merchantId") Long merchantId) throws JsonProcessingException {
        BalanceRequest balanceRequest = new BalanceRequest();
        SysUserEntity merchant = routeService.getSysUser(merchantId);
        String appKey = deptId + "-" + merchantId;
        String secretKey = merchant.getSecretKey();
        return demoClient.balance(balanceRequest, appKey, secretKey);
    }

    // mock
    @PostMapping("chargeNotify")
    public String chargeNotify(@RequestBody String body, @RequestHeader("x-app-key") String appKey, @RequestHeader("x-sign") String sign) throws Exception {
        log.info("收到回调:{}", body);
        String secretKey = routeService.getSysUser(Long.parseLong(appKey.split("-")[1])).getSecretKey();
        return demoClient.chargeNotified(body, sign, appKey, secretKey);
    }

    // mock
    @GetMapping("chargeNotify")
    public String chargeNotifySuccess() throws Exception {
        return "success";
    }

    @PostMapping("withdrawNotify")
    public String withdrawNotify(@RequestBody String body, @RequestHeader("x-app-key") String appKey, @RequestHeader("x-sign") String sign) throws Exception {
        log.info("收到回调:{}", body);
        String secretKey = routeService.getSysUser(Long.parseLong(appKey.split("-")[1])).getSecretKey();
        return demoClient.withdrawNotified(body, sign, appKey, secretKey);
    }

}
