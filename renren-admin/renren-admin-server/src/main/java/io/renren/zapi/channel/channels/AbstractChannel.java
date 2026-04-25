package io.renren.zapi.channel.channels;

import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.renren.commons.tools.exception.RenException;
import io.renren.commons.tools.redis.RedisUtils;
import io.renren.entity.SysUserEntity;
import io.renren.zadmin.dao.ZChargeDao;
import io.renren.zadmin.entity.ZChannelEntity;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zadmin.entity.ZWithdrawEntity;
import io.renren.zapi.ZooConstant;
import io.renren.zapi.channel.PayChannel;
import io.renren.zapi.channel.dto.*;
import lombok.extern.slf4j.Slf4j;

import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

@Slf4j
abstract public class AbstractChannel implements PayChannel {
    private ChannelContext context;
    public static String API_CHARGE_NOTIFY = "chargeNotify";
    public static String API_WITHDRAW_NOTIFY = "withdrawNotify";
    public static String API_CHARGE = "charge";
    public static String API_WITHDRAW = "withdraw";
    public static String API_CHARGE_QUERY = "chargeQuery";
    public static String API_WITHDRAW_QUERY = "withdrawQuery";
    public static String API_BALANCE = "balance";
    public static String API_CREATE_CONTACT = "createContact";
    public static TypeReference<TreeMap<String, Object>> typeReference = new TypeReference<TreeMap<String, Object>>() {
    };

    // 按json解析
    public TreeMap<String, Object> getTreeMap(String body) throws JsonProcessingException {
        return objectMapper().readValue(body, typeReference);
    }

    // 按form表单解析
    public TreeMap<String, Object> getTreeMapByForm(String body) throws JsonProcessingException {
        return this.parseForm(body);
    }

    // 验证签名报文: form
    public TreeMap<String, Object> checkSignByForm(String body, String api) throws JsonProcessingException {
        TreeMap<String, Object> map = getTreeMapByForm(body);

        String sign = (String) map.get(signField());
        map.remove(signField());
        Pair<String, String> pair = getSign(map, api);
        if (!pair.getValue().equals(sign)) {
            log.error("验证签名错误: 对方签名[{}], 我方签名[{}], 我方签名串:[{}]", sign, pair.getValue(), pair.getKey());
            throw new RenException("invalid signature");
        }

        return map;
    }

    public void checkSign(TreeMap<String, Object> map, String api, boolean useEmpty) throws JsonProcessingException {
        String sign = (String) map.get(signField());
        map.remove(signField());
        Pair<String, String> pair = getSign(map, api);
        if (!pair.getValue().equals(sign)) {
            log.error("验证签名错误: 对方签名[{}], 我方签名[{}], 我方签名串:[{}]", sign, pair.getValue(), pair.getKey());
            throw new RenException("invalid signature");
        }
    }

    // 验证签名报文: json
    public TreeMap<String, Object> checkSignByJson(String body, String api) throws JsonProcessingException {
        TreeMap<String, Object> map = getTreeMap(body);
        // 验证签名
        String sign = (String) map.get(signField());
        map.remove(signField());
        Pair<String, String> pair = getSign(map, api);
        if (!pair.getValue().equals(sign)) {
            log.error("验证签名错误: 对方签名[{}], 我方签名[{}], 我方签名串:[{}]", sign, pair.getValue(), pair.getKey());
            throw new RenException("invalid signature");
        }
        return map;
    }

    @Override
    public ChannelContext getContext() {
        return this.context;
    }

    @Override
    public void setContext(ChannelContext context) {
        this.context = context;
    }

    //
    public ChannelChargeResponse syncCharge(ZChargeEntity entity) {
        //////////////////////////////////////////////
        // 同步处理
        //////////////////////////////////////////////
        // 填充map
        TreeMap<String, Object> map = new TreeMap<>();
        setChargeMap(entity, map);
        // 计算并填充签名:  如果getSign返回不为空才需要
        Pair<String, String> sign = getSign(map, API_CHARGE);
        if (sign != null) {
            map.put(signField(), sign.getValue());
        }
        ZChannelEntity channelEntity = channelEntity();

        log.info("球球:{}", channelEntity.getChargeUrl());
        try {
            // 提交服务器
            String resp = this.request(channelEntity.getChargeUrl(), map, "charge");
            log.info("收到: {}", resp);
            // 拿到服务器结果
            JSONObject jsonObject = JSON.parseObject(resp);
            log.info("收到渠道:{}", jsonObject);
            ChannelChargeResponse response = doCharge(jsonObject);
            if (response.getError() != null && sign != null) {
                getContext().error("signstr: {} | sign: {}", sign.getKey(), sign.getValue());
            }
            return response;
        } catch (Exception ex) {
            log.info(ex.getMessage());
            if (sign != null) {
                getContext().error("signstr: {} | sign: {}", sign.getKey(), sign.getValue());
            }
            throw ex;
        }

    }


    @Override
    public ChannelChargeResponse charge(ZChargeEntity entity) {

        // 异步处理
        if (isAsync()) {
            ZChargeEntity current = getContext().getCurrentChargeEntity();

            boolean isDev = getContext().getConfig().isDev();

            String payurl = null;
            if (isDev) {
                payurl = "http://127.0.0.1:7001/sys/landing/async.html?channel=tmo&id=" + current.getId();
            } else {
                payurl = "https://novo.txzfpay.top/sys/landing/async.html?channel=tmo&id=" + current.getId();
            }

            ChannelChargeResponse response = new ChannelChargeResponse();
            response.setPayUrl(payurl);

            RedisUtils redisUtils = getContext().getRedisUtils();
            ZChargeDao chargeDao = getContext().getChargeDao();

            // 异步处理
            CompletableFuture.runAsync(() -> {
                ChannelChargeResponse channelChargeResponse = this.syncCharge(entity);
                String qrcode = this.doChargeAsync(channelChargeResponse, entity.getId());
                String idStr = entity.getId().toString();
                redisUtils.leftPush(idStr.toString(), qrcode);
                redisUtils.expire(idStr, 15);

                // 更新订单号
                if (channelChargeResponse.getChannelOrder() != null) {
                    log.info("更新渠道单号{}:{}", entity.getId(), channelChargeResponse.getChannelOrder());
                    chargeDao.update(
                            Wrappers.<ZChargeEntity>lambdaUpdate()
                                    .eq(ZChargeEntity::getId, entity.getId())
                                    .set(ZChargeEntity::getChannelOrder, channelChargeResponse.getChannelOrder())
                    );
                }
            });

            // 返回
            return response;
        }

        //
        return syncCharge(entity);


    }

    @Override
    public ChannelWithdrawResponse withdraw(ZWithdrawEntity entity, SysUserEntity merchant) {
        // 填充map
        TreeMap<String, Object> map = new TreeMap<>();
        setWithdrawMap(entity, map);

        // 计算并设置签名
        Pair<String, String> pair = getSign(map, API_WITHDRAW);
        if (pair != null) {
            map.put(signField(), pair.getValue());
        }

        // 请求
        String resp = this.request(channelEntity().getWithdrawUrl(), map, "withdraw");
        JSONObject jsonObject = JSON.parseObject(resp);

        // 处理返回结果
        try {
            return doWithdraw(jsonObject);
        } catch (Exception ex) {
            if (pair != null) {
                getContext().error("signstr: {} | sign: {}", pair.getKey(), pair.getValue());
            }
            ChannelWithdrawResponse response = new ChannelWithdrawResponse();
            response.setStatus(ZooConstant.WITHDRAW_STATUS_FAIL);
            response.setError(ex.getMessage());
            return response;
        }
    }

    @Override
    public ChannelChargeQueryResponse chargeQuery(ZChargeEntity entity) {
        // 填充map
        TreeMap<String, Object> map = new TreeMap<>();
        try {
            setChargeQueryMap(entity, map);
        } catch (Exception e) {
            ChannelChargeQueryResponse queryResponse = new ChannelChargeQueryResponse();
            queryResponse.setStatus(ZooConstant.CHARGE_STATUS_PROCESSING);
            return queryResponse;
        }

        // 签名
        Pair<String, String> pair = getSign(map, API_CHARGE_QUERY);
        if (pair != null) {
            map.put(signField(), pair.getValue());
        }

        // 请求
        String resp = this.request(channelEntity().getChargeQueryUrl(), map, "chargeQuery");

        // 处理返回结果
        JSONObject jsonObject = JSON.parseObject(resp);
        try {
            return doChargeQuery(jsonObject);
        } catch (Exception ex) {
            if (pair != null) {
                getContext().error("signstr: {} | sign: {}", pair.getKey(), pair.getValue());
            }
            throw ex;
        }
    }

    @Override
    public ChannelWithdrawResponse withdrawQuery(ZWithdrawEntity entity) {
        // 填充map
        TreeMap<String, Object> map = new TreeMap<>();
        setWithdrawQueryMap(entity, map);

        // 签名
        Pair<String, String> pair = getSign(map, API_WITHDRAW_QUERY);
        if (pair != null) {
            map.put(signField(), pair.getValue());
        }

        // 请求
        String resp = this.request(channelEntity().getWithdrawQueryUrl(), map, "withdrawQuery");

        // 处理
        JSONObject jsonObject = JSON.parseObject(resp);
        try {
            return doWithdrawQuery(jsonObject);
        } catch (Exception ex) {
            if (pair != null) {
                getContext().error("signstr: {} | sign: {}", pair.getKey(), pair.getValue());
            }
            throw ex;
        }
    }


    @Override
    public ChannelBalanceResponse balance() {
        //
        TreeMap<String, Object> map = new TreeMap<>();
        setBalanceMap(map);

        //
        Pair<String, String> pair = getSign(map, API_BALANCE);
        if (pair != null) {
            map.put(signField(), pair.getValue());
        }

        //
        String resp = this.request(channelEntity().getBalanceUrl(), map, "balance");

        //
        JSONObject jsonObject = JSON.parseObject(resp);
        try {
            return doBalance(jsonObject);
        } catch (Exception ex) {
            if (pair != null) {
                getContext().error("signstr: {} | sign: {}", pair.getKey(), pair.getValue());
            }
            throw ex;
        }
    }

    public ChannelBalanceResponse doBalance(JSONObject jsonObject) {
        throw new RenException("not implemented");
    }

    public void setBalanceMap(TreeMap<String, Object> map) {
        throw new RenException("not implemented");
    }

    public ChannelWithdrawResponse doWithdrawQuery(JSONObject jsonObject) {
        throw new RenException("not implemented");
    }

    public void setWithdrawQueryMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        throw new RenException("not implemented");
    }

    public ChannelChargeQueryResponse doChargeQuery(JSONObject jsonObject) {
        throw new RenException("not implemented");
    }

    public void setChargeQueryMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        throw new RenException("not implemented");
    }

    // 如何从json对象里组处标准的渠道应答
    public ChannelWithdrawResponse doWithdraw(JSONObject jsonObject) {
        throw new RenException("not implemented");
    }

    // 如何组代付请求字段
    public void setWithdrawMap(ZWithdrawEntity entity, TreeMap<String, Object> map) {
        throw new RenException("not implemented");
    }

    // 需要实现的: 如何从Json对象里计算处标准的渠道应答
    public ChannelChargeResponse doCharge(JSONObject jsonObject) {
        throw new RenException("not implemented");
    }

    // 如何组渠道需要的字段
    public void setChargeMap(ZChargeEntity entity, TreeMap<String, Object> map) {
        throw new RenException("not implemented");
    }

    // 签名字段的名称
    public String signField() {
        throw new RenException("not implemented");
    }

    // 获取计算签名
    public Pair<String, String> getSign(TreeMap<String, Object> map, String api) {
        throw new RenException("not implemented");
    }

    // 如何请求
    abstract public String request(String url, TreeMap<String, Object> map, String api);
}
