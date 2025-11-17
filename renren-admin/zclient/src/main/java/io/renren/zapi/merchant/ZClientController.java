package io.renren.zapi.merchant;


import com.fasterxml.jackson.core.JsonProcessingException;
import io.renren.zapi.merchant.dto.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("zdemo")
@Slf4j
public class ZClientController {
    @Value("server.port")
    private String port;
    @Value("zclient.appKey")
    private String appKey;
    @Value("zclient.secretKey")
    private String secretKey;
    @Value("zclient.baseUrl")
    private String baseUrl;

    /**
     * 测试付款数据
     */
    @Value("zclient.withdraw.memo")
    private String memo;
    @Value("zclient.withdraw.accountBank")
    private String accountBank;
    @Value("zclient.withdraw.accountNo")
    private String accountNo;
    @Value("zclient.withdraw.accountUser")
    private String accountUser;
    @Value("zclient.withdraw.accountIfsc")
    private String accountIfsc;

    private ApiClient apiClient;

    @PostConstruct
    public void init() {
        this.apiClient = new ApiClient(baseUrl);
    }

    @PostMapping("charge")
    public Result<ChargeResponse> charge(@RequestBody ChargeRequest chargeRequest) throws JsonProcessingException {
        chargeRequest.setOrderId(String.valueOf(new Date().getTime()));
        chargeRequest.setNotifyUrl("http://localhost:" + port + "/zdemo/chargeNotify");
        chargeRequest.setCallbackUrl("http://localhost:" + port + "/zdemo/chargeNotify");
        chargeRequest.setMemo("NA");
        return apiClient.charge(chargeRequest, appKey, secretKey);
    }

    @PostMapping("chargeQuery")
    public Result<ChargeQueryResponse> chargeQuery(@RequestBody ChargeQueryRequest chargeQueryRequest) throws JsonProcessingException {
        return apiClient.chargeQuery(chargeQueryRequest, appKey, secretKey);
    }

    @PostMapping("withdraw")
    public Result<WithdrawResponse> withdraw(@RequestBody WithdrawRequest withdrawRequest) throws JsonProcessingException {
        withdrawRequest.setOrderId(String.valueOf(new Date().getTime()));
        withdrawRequest.setNotifyUrl("http://localhost:" + port + "/zdemo/withdrawNotify");
        withdrawRequest.setCallbackUrl("http://localhost:" + port + "/zdemo/withdrawNotify");
        // 填充测试数据 | fill in the test data
        withdrawRequest.setMemo(memo);
        withdrawRequest.setAccountBank(accountBank);
        withdrawRequest.setAccountNo(accountNo);
        withdrawRequest.setAccountUser(accountUser);
        withdrawRequest.setAccountIfsc(accountIfsc);
        return apiClient.withdraw(withdrawRequest, appKey, secretKey);
    }

    @PostMapping("withdrawQuery")
    public Result<WithdrawQueryResponse> withdrawQuery(@RequestBody WithdrawQueryRequest withdrawQueryRequest) throws JsonProcessingException {
        return apiClient.withdrawQuery(withdrawQueryRequest, appKey, secretKey);
    }

    @PostMapping("balance")
    public Result<BalanceResponse> balance(@RequestBody BalanceRequest balanceRequest) throws JsonProcessingException {
        return apiClient.balance(balanceRequest, appKey, secretKey);
    }

    @PostMapping("chargeNotify")
    public String chargeNotify(@RequestBody String body, @RequestHeader("x-sign") String sign) throws Exception {
        log.info("收到回调:{}", body);
        return apiClient.chargeNotified(body, sign, appKey, secretKey);
    }

    @GetMapping("chargeNotify")
    public String chargeNotifySuccess() throws Exception {
        log.info("chargeNotify success");
        return "success";
    }

    @PostMapping("withdrawNotify")
    public String withdrawNotify(@RequestBody String body, @RequestHeader("x-sign") String sign) throws Exception {
        log.info("收到回调:{}", body);
        return apiClient.withdrawNotified(body, sign, appKey, secretKey);
    }

}
