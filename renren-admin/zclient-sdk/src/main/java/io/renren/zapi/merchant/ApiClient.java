package io.renren.zapi.merchant;

import cn.hutool.crypto.digest.DigestUtil;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.renren.zapi.merchant.dto.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class ApiClient {

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    private String baseUrl;

    private static String chargeUrl = "/sys/zapi/charge";
    private static String chargeQueryUrl = "/sys/zapi/chargeQuery";
    private static String withdrawUrl = "/sys/zapi/withdraw";
    private static String withdrawQueryUrl = "/sys/zapi/withdrawQuery";
    private static String balanceUrl = "/sys/zapi/balance";

    private static TypeReference<Result<ChargeResponse>> chargeType = new TypeReference<Result<ChargeResponse>>() {
    };
    private static TypeReference<Result<ChargeQueryResponse>> chargeQueryType = new TypeReference<Result<ChargeQueryResponse>>() {
    };
    private static TypeReference<Result<WithdrawResponse>> withdrawType = new TypeReference<Result<WithdrawResponse>>() {
    };
    private static TypeReference<Result<WithdrawQueryResponse>> withdrawQueryType = new TypeReference<Result<WithdrawQueryResponse>>() {
    };
    private static TypeReference<Result<BalanceResponse>> balanceType = new TypeReference<Result<BalanceResponse>>() {
    };
    private static TypeReference<Result<ChargeNotify>> chargeNotifyType = new TypeReference<Result<ChargeNotify>>() {
    };
    private static TypeReference<Result<WithdrawNotify>> withdrawNotifyType = new TypeReference<Result<WithdrawNotify>>() {
    };

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, new ToStringSerializer());
        module.addDeserializer(Long.class, new JsonDeserializer<Long>() {
            @Override
            public Long deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
                String value = jsonParser.getText();
                if (value == null || value.isBlank()) {
                    return null;
                }
                try {
                    return Long.valueOf(value);
                } catch (NumberFormatException ex) {
                    throw new JsonParseException(jsonParser, "Invalid Long value:" + value, jsonParser.currentLocation(), ex);
                }
            }
        });
        objectMapper.registerModule(module);
    }

    private String request(Object obj, String uri, String appKey, String secretKey) throws JsonProcessingException {
        String body = objectMapper.writeValueAsString(obj);
        String sign = DigestUtil.md5Hex(body + secretKey);
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-app-key", appKey);
        headers.add("x-sign", sign);
        RequestEntity<String> requestEntity = RequestEntity.post("")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL)
                .acceptCharset(StandardCharsets.UTF_8)
                .headers(headers)
                .body(body);

        String url = baseUrl + uri;
        String resBody = null;
        try {
            ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity(url, requestEntity, String.class);
            resBody = stringResponseEntity.getBody();
            return resBody;
        } catch (Exception ex) {
            System.out.println("req url = " + url);
            System.out.println("req body = " + requestEntity.getBody());
            System.out.println("req header = x-app-key[" + appKey + "], x-sign[" + sign + "]");
            System.out.println("res body = " + resBody);
            throw ex;
        }

    }

    public Result<ChargeResponse> charge(ChargeRequest chargeRequest, String appKey, String secretKey) throws JsonProcessingException {
        String resp = request(chargeRequest, chargeUrl, appKey, secretKey);
        Result<ChargeResponse> chargeResponseResult = objectMapper.readValue(resp, chargeType);

        // 顺便发起一次查询测试
        ChargeQueryRequest chargeQueryRequest = new ChargeQueryRequest();
        chargeQueryRequest.setOrderId(chargeRequest.getOrderId());
        this.chargeQuery(chargeQueryRequest, appKey, secretKey);
        return chargeResponseResult;
    }

    public Result<ChargeQueryResponse> chargeQuery(ChargeQueryRequest chargeQueryRequest, String appKey, String secretKey) throws JsonProcessingException {
        String resp = request(chargeQueryRequest, chargeQueryUrl, appKey, secretKey);
        return objectMapper.readValue(resp, chargeQueryType);
    }

    public Result<WithdrawResponse> withdraw(WithdrawRequest withdrawRequest, String appKey, String secretKey) throws JsonProcessingException {
        String resp = request(withdrawRequest, withdrawUrl, appKey, secretKey);
        Result<WithdrawResponse> withdrawResponseResult = objectMapper.readValue(resp, withdrawType);

        // 顺便发起一次查询测试
        WithdrawQueryRequest req = new WithdrawQueryRequest();
        req.setOrderId(withdrawRequest.getOrderId());
        this.withdrawQuery(req, appKey, secretKey);
        return withdrawResponseResult;
    }

    public Result<WithdrawQueryResponse> withdrawQuery(WithdrawQueryRequest withdrawQueryRequest, String appKey, String secretKey) throws JsonProcessingException {
        String resp = request(withdrawQueryRequest, withdrawQueryUrl, appKey, secretKey);
        return objectMapper.readValue(resp, withdrawQueryType);

    }

    public Result<BalanceResponse> balance(BalanceRequest balanceRequest, String appKey, String secretKey) throws JsonProcessingException {
        String resp = request(balanceRequest, balanceUrl, appKey, secretKey);
        return objectMapper.readValue(resp, balanceType);
    }

    public String chargeNotified(String body, String sign, String appKey, String secretKey) throws Exception {
        String signStr = body + secretKey;
        String calc = DigestUtil.md5Hex(signStr);
        if(!calc.equals(sign)) {
            String msg = String.format("verify signature failed: signstr=[%s] calc=[%s] server sign[%s]", signStr, calc, sign);
            throw new Exception(msg);
        }
        System.out.println("chargeNotified, body=[" + body + "]");
        Result<ChargeNotify> chargeNotifyResult = objectMapper.readValue(body, chargeNotifyType);
        return "OK";
    }

    public String withdrawNotified(String body, String sign, String appKey, String secretKey) throws Exception {
        String calc = DigestUtil.md5Hex(body + secretKey);
        if(!calc.equals(sign)) {
            throw new Exception("sign mismatch");
        }
        Result<WithdrawNotify> withdrawNotifyResult = objectMapper.readValue(body, withdrawNotifyType);
        return "OK";
    }


}
