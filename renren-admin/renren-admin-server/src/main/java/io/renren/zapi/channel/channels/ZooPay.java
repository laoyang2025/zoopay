package io.renren.zapi.channel.channels;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.renren.commons.tools.exception.RenException;
import io.renren.commons.tools.utils.ConvertUtils;
import io.renren.entity.SysUserEntity;
import io.renren.zadmin.entity.ZChannelEntity;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zadmin.entity.ZWithdrawEntity;
import io.renren.zapi.ZooConstant;
import io.renren.zapi.channel.dto.*;
import io.renren.zapi.merchant.ApiClient;
import io.renren.zapi.merchant.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.TreeMap;

// 对接zoopay渠道
@Slf4j
public class ZooPay extends PostJsonChannel {

    private ApiClient apiClient;

    /**
     * @param context
     */
    @Override
    public void setContext(ChannelContext context) {
        super.setContext(context);
        String balanceUrl = context.getChannelEntity().getBalanceUrl();
        int pathPos = balanceUrl.indexOf('/', 10);
        String baseUrl = balanceUrl.substring(0, pathPos);
        apiClient = new ApiClient(baseUrl);
    }

    /*
     * @param chargeEntity
     * @return
     */
    @Override
    public ChannelChargeResponse charge(ZChargeEntity chargeEntity) {
        ChargeRequest chargeRequest = ConvertUtils.sourceToTarget(chargeEntity, ChargeRequest.class);
        chargeRequest.setOrderId(chargeEntity.getId().toString());
        chargeRequest.setNotifyUrl(this.getCollectNotifyUrl(chargeEntity));
        ZChannelEntity channelEntity = this.channelEntity();
        try {
            Result<ChargeResponse> resp = apiClient.charge(chargeRequest, channelEntity.getMerchantId(), channelEntity().getPrivateKey());
            int code = resp.getCode();
            if (code == 0) {
                ChargeResponse data = resp.getData();
                ChannelChargeResponse response = ConvertUtils.sourceToTarget(data, ChannelChargeResponse.class);
                response.setChannelOrder(data.getId().toString());
                return response;
            } else {
                ChannelChargeResponse response = new ChannelChargeResponse();
                response.setError("渠道错误:" + resp.getMsg());
                return response;
            }
        } catch (JsonProcessingException e) {
            throw new RenException("请求渠道失败");
        }
    }

    /**
     * @param chargeEntity
     * @return
     */
    @Override
    public ChannelChargeQueryResponse chargeQuery(ZChargeEntity chargeEntity) {
        ZChannelEntity channelEntity = this.channelEntity();
        ChargeQueryRequest query = ConvertUtils.sourceToTarget(chargeEntity, ChargeQueryRequest.class);
        query.setOrderId(chargeEntity.getId().toString());
        query.setId(null);
        try {
            Result<ChargeQueryResponse> result = apiClient.chargeQuery(query, channelEntity.getMerchantId(), channelEntity.getPrivateKey());
            if (result.getCode() == 0) {
                ChargeQueryResponse data = result.getData();
                ChannelChargeQueryResponse response = ConvertUtils.sourceToTarget(data, ChannelChargeQueryResponse.class);
                response.setStatus(data.getProcessStatus());
                return response;
            } else {
                ChannelChargeQueryResponse response = new ChannelChargeQueryResponse();
                response.setError("渠道错误:" + result.getMsg());
                response.setStatus(ZooConstant.CHARGE_STATUS_PROCESSING);
                return response;
            }
        } catch (JsonProcessingException e) {
            throw new RenException("请求渠道失败");
        }
    }

    /**
     * 发起提现
     *
     * @param withdrawEntity
     * @return
     */
    @Override
    public ChannelWithdrawResponse withdraw(ZWithdrawEntity withdrawEntity, SysUserEntity merchant) {
        ZChannelEntity channelEntity = this.channelEntity();
        WithdrawRequest request = ConvertUtils.sourceToTarget(withdrawEntity, WithdrawRequest.class);
        request.setNotifyUrl(this.getWithdrawNotifyUrl(withdrawEntity));
        request.setOrderId(withdrawEntity.getId().toString());
        try {
            Result<WithdrawResponse> result = apiClient.withdraw(request, channelEntity.getMerchantId(), channelEntity.getPrivateKey());
            if (result.getCode() == 0) {
                WithdrawResponse data = result.getData();
                ChannelWithdrawResponse response = ConvertUtils.sourceToTarget(data, ChannelWithdrawResponse.class);
                response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
                return response;
            } else {
                ChannelWithdrawResponse response = new ChannelWithdrawResponse();
                response.setError("渠道错误:" + result.getMsg());
                response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
                return response;
            }
        } catch (JsonProcessingException e) {
            throw new RenException("请求渠道失败");
        }
    }

    /**
     * 发起提现查询
     *
     * @return
     */
    @Override
    public ChannelWithdrawResponse withdrawQuery(ZWithdrawEntity withdrawEntity) {
        ZChannelEntity channelEntity = this.channelEntity();
        WithdrawQueryRequest request = ConvertUtils.sourceToTarget(withdrawEntity, WithdrawQueryRequest.class);
        request.setOrderId(withdrawEntity.getId().toString());
        request.setId(null);
        try {
            Result<WithdrawQueryResponse> result = apiClient.withdrawQuery(request, channelEntity.getMerchantId(), channelEntity.getPrivateKey());
            if (result.getCode() == 0) {
                WithdrawQueryResponse data = result.getData();
                ChannelWithdrawResponse response = ConvertUtils.sourceToTarget(data, ChannelWithdrawResponse.class);
                response.setStatus(data.getProcessStatus());
                return response;
            } else {
                ChannelWithdrawResponse response = new ChannelWithdrawResponse();
                response.setError("渠道错误:" + result.getMsg());
                response.setStatus(ZooConstant.WITHDRAW_STATUS_ASSIGNED);
                return response;
            }
        } catch (JsonProcessingException e) {
            throw new RenException("请求渠道失败");
        }
    }

    /**
     * 余额查询
     *
     * @return
     */
    @Override
    public ChannelBalanceResponse balance() {
        ZChannelEntity channelEntity = channelEntity();
        BalanceRequest request = new BalanceRequest();
        try {
            Result<BalanceResponse> result = apiClient.balance(request, channelEntity.getMerchantId(), channelEntity.getPrivateKey());
            if (result.getCode() == 0) {
                BalanceResponse data = result.getData();
                ChannelBalanceResponse response = new ChannelBalanceResponse();
                response.setBalance(data.getBalance());
                response.setBalanceMemo(data.getBalance().toString());
                return response;
            } else {
                throw new RenException("渠道错误:" + result.getMsg());
            }
        } catch (JsonProcessingException e) {
            throw new RenException("请求渠道失败");
        }
    }

    /**
     *
     */
    @Override
    public ChannelChargeQueryResponse chargeNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZChargeEntity chargeEntity) throws IOException {
        String bodyStr = (String)body;
        JSONObject jsonObject = JSON.parseObject(bodyStr);
        ChannelChargeQueryResponse resp = new ChannelChargeQueryResponse();
        Integer processStatus = jsonObject.getInteger("processStatus");
        resp.setStatus(processStatus);
        resp.setUtr(jsonObject.getString("utr"));
        return resp;
    }


    /**
     * 返回标准的代付状态
     */
    @Override
    public ChannelWithdrawResponse drawNotified(String contentType, Object body, Long deptId, Long id, HttpServletRequest request, HttpServletResponse response, ZWithdrawEntity withdrawEntity) throws IOException {
        String bodyStr = (String)body;
        JSONObject jsonObject = JSON.parseObject(bodyStr);
        ChannelWithdrawResponse resp = new ChannelWithdrawResponse();
        Integer processStatus = jsonObject.getInteger("processStatus");
        resp.setStatus(processStatus);
        return resp;
    }

    // 充值通知应答渠道的body
    public String responseChargeOk() {
        return "SUCCESS";
    }

    // 充值通知应答渠道的body
    public String responseWithdrawOk() {
        return "SUCCESS";
    }

}
