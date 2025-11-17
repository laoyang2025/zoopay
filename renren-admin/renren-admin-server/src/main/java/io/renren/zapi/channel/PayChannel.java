package io.renren.zapi.channel;

import cn.hutool.core.lang.Pair;
import cn.hutool.crypto.digest.DigestUtil;
import com.bstek.ureport.export.html.HtmlProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.renren.commons.tools.exception.RenException;
import io.renren.entity.SysUserEntity;
import io.renren.zapi.ZConfig;
import io.renren.zapi.ledger.ZLedger;
import io.renren.zapi.channel.dto.*;
import io.renren.zapi.merchant.ApiContext;
import io.renren.zapi.utils.CommonUtils;
import io.renren.zadmin.dao.ZChargeDao;
import io.renren.zadmin.dao.ZWithdrawDao;
import io.renren.zadmin.entity.ZChannelEntity;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zadmin.entity.ZWithdrawEntity;
import io.renren.zapi.ZooConstant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.*;

public interface PayChannel {
    /**
     * @return
     */
    ChannelContext getContext();

    default ZChannelEntity channelEntity() {
        return getContext().getChannelEntity();
    }

    default RestTemplate restTemplate() {
        return getContext().getRestTemplate();
    }

    default ZConfig config() {
        return getContext().getConfig();
    }

    default ZLedger ledger() {
        return getContext().getLedger();
    }

    default ObjectMapper objectMapper() {
        return getContext().getObjectMapper();
    }

    default ZChargeDao chargeDao() {
        return getContext().getChargeDao();
    }

    default ZWithdrawDao withdrawDao() {
        return getContext().getWithdrawDao();
    }

    /**
     * @param context
     */
    void setContext(ChannelContext context);

    /**
     * 是否为本地渠道
     *
     * @return
     */
    default boolean isLocal() {
        return false;
    }

    default boolean isChargeEnabled() {
        return 1 == getContext().getChannelEntity().getChargeEnabled();
    }

    default boolean isWithdrawEnabled() {
        return 1 == getContext().getChannelEntity().getWithdrawEnabled();
    }

    /**
     * @param chargeEntity
     * @return
     */
    default ChannelChargeResponse charge(ZChargeEntity chargeEntity) {
        throw new RenException("not implemented");
    }

    /**
     * @param chargeEntity
     * @return
     */
    default ChannelChargeQueryResponse chargeQuery(ZChargeEntity chargeEntity) {
        throw new RenException("not implemented");
    }

    /**
     * 发起提现
     *
     * @param withdrawEntity
     * @return
     */
    default ChannelWithdrawResponse withdraw(ZWithdrawEntity withdrawEntity, SysUserEntity merchant) {
        throw new RenException("not implemented");
    }

    /**
     * 发起提现查询
     *
     * @param faWithdrawEntity
     * @return
     */
    default ChannelWithdrawResponse withdrawQuery(ZWithdrawEntity faWithdrawEntity) {
        throw new RenException("not implemented");
    }

    /**
     * 余额查询
     *
     * @return
     */
    default ChannelBalanceResponse balance() {
        throw new RenException("not implemented");
    }

    /**
     * 充值回调
     *
     * @param contentType
     * @param body
     * @param deptId
     * @param id
     * @param response
     * @param chargeEntity
     * @return
     * @throws IOException
     */
    default ChannelChargeQueryResponse chargeNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZChargeEntity chargeEntity) throws IOException {
        throw new RenException("not implemented");
    }

    /**
     * 代付回调
     *
     * @param contentType
     * @param body
     * @param deptId
     * @param id
     * @param response
     * @param withdrawEntity
     * @return
     * @throws IOException
     */
    default ChannelWithdrawResponse drawNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZWithdrawEntity withdrawEntity) throws IOException {
        throw new RemoteException("not implemented");
    }

    /**
     *
     */
    default Pair<String, Object> webhook(Long deptId, Long channelId, String contentType, String body, HttpServletRequest request, HttpServletResponse response) {
        throw new RenException("webhook unsupported");
    }

    /**
     * 充值回调地址
     *
     * @param chargeEntity
     * @return
     */
    default String getCollectNotifyUrl(ZChargeEntity chargeEntity) {
        ChannelContext context = getContext();
        ZChannelEntity channelEntity = context.getChannelEntity();
        String domain = context.getDept().getApiDomain();
        return String.format("%s/sys/zchannel/charge/%d/%s/%d",
                domain,
                channelEntity.getDeptId(),
                channelEntity.getId(),
                chargeEntity.getId()
        );
    }

    /**
     * 代付回调地址
     *
     * @param entity
     * @return
     */
    default String getWithdrawNotifyUrl(ZWithdrawEntity entity) {
        ChannelContext context = getContext();
        ZChannelEntity channelEntity = context.getChannelEntity();
        String domain = context.getDept().getApiDomain();
        return String.format("%s/sys/zchannel/withdraw/%d/%s/%d",
                domain,
                channelEntity.getDeptId(),
                channelEntity.getId(),
                entity.getId()
        );
    }

    /**
     * 解析form
     *
     * @param urlEncodedString
     * @return
     */
    default TreeMap<String, Object> parseForm(String urlEncodedString) {
        TreeMap<String, Object> paramsMap = new TreeMap<>();
        String[] params = urlEncodedString.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            try {
                String key = URLDecoder.decode(keyValue[0], "UTF-8");
                String value = null;
                if (keyValue.length > 1) {
                    value = URLDecoder.decode(keyValue[1], "UTF-8");
                }
                paramsMap.put(key, value);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return paramsMap;
    }

    default String getForm(String url, TreeMap<String, Object> map) {
        RestTemplate restTemplate = getContext().getRestTemplate();
        HttpHeaders headers = new HttpHeaders();

        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        for (HttpMessageConverter<?> messageConverter : messageConverters) {
            if (messageConverter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) messageConverter).setDefaultCharset(Charset.forName("UTF8"));
            }
        }

        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            try {
                String encodedKey = URLEncoder.encode(entry.getKey(), "UTF-8");
                String encodedValue = URLEncoder.encode(entry.getValue().toString(), "UTF-8");
                if (queryString.length() > 0) {
                    queryString.append("&");
                }
                queryString.append(encodedKey).append("=").append(encodedValue);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                throw new RenException("can not encode url");
            }
        }
        url = url + "?" + queryString;
        getContext().info("send: {}", url);

        ResponseEntity<String> forEntity = restTemplate.getForEntity(url, String.class);
        String respStr = forEntity.getBody();

        getContext().info("recv: {}", respStr);
        return respStr;
    }




    default String getForm(String url, TreeMap<String, Object> map, HttpHeaders headers) {
        RestTemplate restTemplate = getContext().getRestTemplate();
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        for (HttpMessageConverter<?> messageConverter : messageConverters) {
            if (messageConverter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) messageConverter).setDefaultCharset(Charset.forName("UTF8"));
            }
        }
        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            try {
                String encodedKey = URLEncoder.encode(entry.getKey(), "UTF-8");
                String encodedValue = URLEncoder.encode(entry.getValue().toString(), "UTF-8");
                if (queryString.length() > 0) {
                    queryString.append("&");
                }
                queryString.append(encodedKey).append("=").append(encodedValue);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                throw new RenException("can not encode url");
            }
        }
        url = url + "?" + queryString;
        getContext().info("send: {}", url);
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> forEntity = restTemplate.exchange(url, HttpMethod.GET,request, String.class);
        String respStr = forEntity.getBody();
        getContext().info("recv: {}", respStr);
        return respStr;
    }

    /**
     * 发送application/form_urlencoded
     *
     * @param url
     * @param map
     * @return
     */
    default String postForm(String url, TreeMap<String, Object> map, HttpHeaders inHeaders) {
        RestTemplate restTemplate = getContext().getRestTemplate();
        HttpHeaders headers = inHeaders == null ? new HttpHeaders() : inHeaders;
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        for (HttpMessageConverter<?> messageConverter : messageConverters) {
            if (messageConverter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) messageConverter).setDefaultCharset(Charset.forName("UTF8"));
            }
        }
        try {
            getContext().info("send: {}", objectMapper().writeValueAsString(map));
        } catch (JsonProcessingException e) {
            throw new RenException("serialize error");
        }
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            multiValueMap.add(entry.getKey(), entry.getValue());
        }
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(multiValueMap, headers);
        try {
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, request, String.class);
            String respStr = responseEntity.getBody();
            getContext().info("recv: {}", respStr);
            return respStr;
        } catch (Exception ex) {
            throw new RenException(ex.getMessage());
        }
    }

    /**
     * 发送multipart/form-data请求
     *
     * @param url
     * @param map
     * @return
     */
    default String postMultipart(String url, Map<String, Object> map) {
        RestTemplate restTemplate = getContext().getRestTemplate();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        for (HttpMessageConverter<?> messageConverter : messageConverters) {
            if (messageConverter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) messageConverter).setDefaultCharset(Charset.forName("UTF8"));
            }
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            body.add(entry.getKey(), entry.getValue());
        }
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String response = restTemplate.postForObject(url, requestEntity, String.class);
        return response;
    }

    /**
     * 发送post json请求
     *
     * @param url
     * @param map
     * @return
     */
    default String postJSON(String url, Map<String, Object> map) {
        ObjectMapper objectMapper = getContext().getObjectMapper();
        RestTemplate restTemplate = getContext().getRestTemplate();
        RequestEntity requestEntity = null;
        String bodyStr;
        try {
            bodyStr = objectMapper.writeValueAsString(map);
            requestEntity = RequestEntity.post("")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.ALL)
                    .acceptCharset(StandardCharsets.UTF_8)
                    .body(bodyStr);
        } catch (JsonProcessingException ex) {
            throw new RenException("invalid request");
        }
        getContext().info("send: {} to {}", bodyStr, url);
        try {
            ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity(url, requestEntity, String.class);
            String body = stringResponseEntity.getBody();
            getContext().info("recv: {}", body);
            return body;
        } catch ( Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    default String postJSON(String url, Map<String, Object> map, HttpHeaders headers) {
        ObjectMapper objectMapper = getContext().getObjectMapper();
        RestTemplate restTemplate = getContext().getRestTemplate();
        RequestEntity requestEntity = null;
        String bodyStr = null;
        try {
            bodyStr = objectMapper.writeValueAsString(map);
            requestEntity = RequestEntity.post("")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.ALL)
                    .acceptCharset(StandardCharsets.UTF_8)
                    .headers(headers)
                    .body(bodyStr);
        } catch (JsonProcessingException ex) {
            throw new RenException("invalid request");
        }
        getContext().info("send: to      {}", url);
        getContext().info("send: headers {}", headers);
        getContext().info("send: body    {}", bodyStr);
        ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity(url, requestEntity, String.class);
        String body = stringResponseEntity.getBody();
        getContext().info("recv: {}", body);
        return body;
    }

    default String getJSON(String url, Map<String, Object> map, HttpHeaders headers) {
        ObjectMapper objectMapper = getContext().getObjectMapper();
        RestTemplate restTemplate = getContext().getRestTemplate();
        RequestEntity requestEntity = null;
        String bodyStr = null;
        try {
            bodyStr = objectMapper.writeValueAsString(map);
            requestEntity = RequestEntity.post("")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.ALL)
                    .acceptCharset(StandardCharsets.UTF_8)
                    .headers(headers)
                    .body(bodyStr);
        } catch (JsonProcessingException ex) {
            throw new RenException("invalid request");
        }
        getContext().info("send: {} to {}, headers:{}", bodyStr, url, headers);
//        ResponseEntity<String> stringResponseEntity = restTemplate.getForEntity(url, requestEntity, String.class);
        ResponseEntity<String> stringResponseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        String body = stringResponseEntity.getBody();
        getContext().info("recv: {}", body);
        return body;
    }

    default String postJSONString(String url, String body, HttpHeaders headers) {
        ObjectMapper objectMapper = getContext().getObjectMapper();
        RestTemplate restTemplate = getContext().getRestTemplate();
        RequestEntity requestEntity = null;
        String bodyStr = null;
        requestEntity = RequestEntity.post("")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL)
                .acceptCharset(StandardCharsets.UTF_8)
                .headers(headers)
                .body(body);
        getContext().info("send: {} to {}, headers:{}", bodyStr, url, headers);
        ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity(url, requestEntity, String.class);
        String bodyResp = stringResponseEntity.getBody();
        getContext().info("recv: {}", bodyResp);
        return bodyResp;
    }

    default String md5SignString(TreeMap<String, Object> map, boolean useEmpty) {
        StringBuilder sb = new StringBuilder();
        Set<Map.Entry<String, Object>> entries = map.entrySet();
        int cur = 0;
        for (Map.Entry<String, Object> entry : entries) {
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (value instanceof String && !useEmpty && StringUtils.isBlank((String) value)) {
                continue;
            }
            if (cur != 0) {
                sb.append("&");
            }
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue().toString());
            cur++;
        }
        return sb.toString();
    }

    default Pair<String, String> kvMd5Sign(TreeMap<String, Object> map, String md5Key, String keyName, boolean useEmpty) {
        String signStr = md5SignString(map, useEmpty);
        ChannelContext ctx = this.getContext();
        ZChannelEntity channelEntity = ctx.getChannelEntity();
        String keyValue = md5Key == null ? channelEntity.getPrivateKey() : md5Key;
        if (keyName == null) {
            keyName = "key";
        }
        signStr = signStr + "&" + keyName + "=" + keyValue;
        String sign = DigestUtil.md5Hex(signStr).toUpperCase();
        return Pair.of(signStr, sign);
    }

    /**
     * ip白名单校验
     */
    default void checkIp() {
        String whiteIp = this.getContext().getChannelEntity().getWhiteIp();
        String ip = CommonUtils.getIp();
        if (!whiteIp.contains(ip)) {
            throw new RenException(ip + " is not in white list[" + whiteIp + "]");
        }
    }

    /**
     * 充值回调应答
     *
     * @param response
     * @param status
     */
    default void responseCharge(HttpServletResponse response, int status) {
        try {
            if (status == ZooConstant.CHARGE_STATUS_SUCCESS) {
                response.getWriter().write(responseChargeOk());
                return;
            }
            response.getWriter().write(responseChargeFail());
        } catch (IOException e) {
            throw new RenException("write response failed");
        }
    }

    /**
     * 代付回调应答
     *
     * @param response
     * @param status
     */
    default void responseWithdraw(HttpServletResponse response, int status) {
        try {
            if (status == ZooConstant.WITHDRAW_STATUS_SUCCESS) {
                response.getWriter().write(responseWithdrawOk());
                return;
            }
            response.getWriter().write(responseWithdrawFail());
        } catch (IOException e) {
            throw new RenException("write response failed");
        }
    }

    default String responseChargeOk() {
        return "OK";
    }

    default String responseChargeFail() {
        throw new RenException("default fail");
    }

    default String responseWithdrawOk() {
        return "OK";
    }

    default String responseWithdrawFail() {
        throw new RenException("default fail");
    }

    default String jumpHandle(Map<String, String> map) {
        ZChannelEntity channelEntity = channelEntity();
        StringBuilder sb = new StringBuilder();
        map.forEach((k, v) -> {
            sb.append(String.format("<input type='hidden' name='%s' value='%s' />", k, v));
        });
        String pageHtml = String.format("""
                        <html>
                         <head>
                         </head>
                         <body onload='document.forms[0].submit()'>
                         <form method='POST' action='%s'>
                         %s
                         <input type='submit'/>
                         </form>
                         </body>
                        </html>
                        """,
                channelEntity.getChargeUrl(),
                sb
        );
        return pageHtml;
    }
}
