package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.commons.tools.utils.ConvertUtils;
import io.renren.zadmin.dao.ZChargeDao;
import io.renren.zadmin.dto.ZChargeDTO;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zadmin.service.ZChargeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * z_charge
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Service
public class ZChargeServiceImpl extends CrudServiceImpl<ZChargeDao, ZChargeEntity, ZChargeDTO> implements ZChargeService {

    @DataFilter
    @Override
    public PageData<ZChargeDTO> page(Map<String, Object> params) {
        IPage<ZChargeEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, ZChargeDTO.class);
    }

    @DataFilter
    @Override
    public List<ZChargeDTO> list(Map<String, Object> params) {
        List<ZChargeEntity> list = baseDao.selectList(
                applyFilter(params)
        );
        return ConvertUtils.sourceToTarget(list, ZChargeDTO.class);
    }

    @Override
    public QueryWrapper<ZChargeEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<ZChargeEntity> wrapper = new QueryWrapper<>();

        String orderId = (String)params.get("orderId");
        if(StringUtils.isNotBlank(orderId)) {
            wrapper.eq("order_id", orderId);
        }

        String id = (String)params.get("id");
        if(StringUtils.isNotBlank(id)) {
            wrapper.eq("id", Long.parseLong(id));
        }

        String upi = (String)params.get("upi");
        if (StringUtils.isNotBlank(upi)) {
            wrapper.eq("upi", upi);
        }

        String utr = (String)params.get("utr");
        if(StringUtils.isNotBlank(utr)) {
            wrapper.eq("utr", utr);
        }

        String handleMode = (String)params.get("handleMode");
        if(StringUtils.isNotBlank(handleMode) ) {
            wrapper.eq("handle_mode", handleMode);
        }

        String notifyStatus = (String)params.get("notifyStatus");
        if(StringUtils.isNotBlank(notifyStatus)) {
            wrapper.eq("notify_status", Integer.parseInt(notifyStatus));
        }

        String processStatus = (String)params.get("processStatus");
        if(StringUtils.isNotBlank(processStatus)) {
            wrapper.eq("process_status", Integer.parseInt(processStatus));
        }

        String channelId = (String)params.get("channelId");
        if(StringUtils.isNotBlank(channelId)) {
            wrapper.eq("channel_id", Long.parseLong(channelId));
        }

        String antId = (String)params.get("antId");
        if(StringUtils.isNotBlank(antId)) {
            wrapper.eq("ant_id", Long.parseLong(antId));
        }

        String userId = (String)params.get("userId");
        if(StringUtils.isNotBlank(userId)) {
            wrapper.eq("user_id", Long.parseLong(userId));
        }

        String agentId = (String)params.get("agentId");
        if(StringUtils.isNotBlank(agentId)) {
            wrapper.eq("agent_id", Long.parseLong(agentId));
        }

        String middleId = (String)params.get("middleId");
        if(StringUtils.isNotBlank(middleId)) {
            wrapper.eq("middle_id", Long.parseLong(middleId));
        }

        String merchantId = (String)params.get("merchantId");
        if(StringUtils.isNotBlank(merchantId)) {
            wrapper.eq("merchant_id", Long.parseLong(merchantId));
        }

        String startDate = (String) params.get("startDate");
        if (StringUtils.isNotBlank((String) params.get("startDate"))) {
            wrapper.ge("create_date", startDate);
        }

        String endDate = (String) params.get("endDate");
        if (StringUtils.isNotBlank((String) params.get("endDate"))) {
            wrapper.le("create_date", endDate);
        }

        String cardId = (String) params.get("cardId");
        if (StringUtils.isNotBlank(cardId)) {
            wrapper.eq("card_id", Long.parseLong(cardId));
        }

        // 查看共享流水
        String shareView = (String)params.get("shareView");
        if ("1".equals(shareView)) {
            wrapper.gt("agent_share", 0);
        }

        // 如果是拓展方登录


//        System.out.println("chargeService wrapper: " + wrapper.getSqlSelect());
        return wrapper;
    }


}