package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.commons.tools.utils.ConvertUtils;
import io.renren.zadmin.dao.ZWithdrawDao;
import io.renren.zadmin.dto.ZWithdrawDTO;
import io.renren.zadmin.entity.ZWithdrawEntity;
import io.renren.zadmin.service.ZWithdrawService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * z_withdraw
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Service
public class ZWithdrawServiceImpl extends CrudServiceImpl<ZWithdrawDao, ZWithdrawEntity, ZWithdrawDTO> implements ZWithdrawService {

    @DataFilter
    @Override
    public PageData<ZWithdrawDTO> page(Map<String, Object> params) {
        IPage<ZWithdrawEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, ZWithdrawDTO.class);
    }

    @DataFilter
    @Override
    public List<ZWithdrawDTO> list(Map<String, Object> params) {
        List<ZWithdrawEntity> list = baseDao.selectList(
                applyFilter(params)
        );
        return ConvertUtils.sourceToTarget(list, ZWithdrawDTO.class);
    }

    @Override
    public QueryWrapper<ZWithdrawEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<ZWithdrawEntity> wrapper = new QueryWrapper<>();

        String id = (String)params.get("id");
        wrapper.eq(StringUtils.isNotBlank(id), "id", id);

        String merchantId = (String)params.get("merchantId");
        if (StringUtils.isNotBlank(merchantId)) {
            wrapper.eq( "merchant_id", Long.parseLong(merchantId));
        }

        String processStatus = (String)params.get("processStatus");
        if (StringUtils.isNotBlank(processStatus)) {
            wrapper.eq( "process_status", Integer.parseInt(processStatus));
        }

        String orderId = (String)params.get("orderId");
        wrapper.eq(StringUtils.isNotBlank(orderId), "order_id", orderId);

        String logId = (String)params.get("logId");
        wrapper.eq(StringUtils.isNotBlank(logId), "log_id", logId);

        String channelId = (String)params.get("channelId");
        wrapper.eq(StringUtils.isNotBlank(channelId), "channel_id", channelId);

        String channelOrder = (String)params.get("channelOrder");
        wrapper.eq(StringUtils.isNotBlank(channelOrder), "channel_order", channelOrder);

        String cardUser = (String)params.get("cardUser");
        wrapper.eq(StringUtils.isNotBlank(cardUser), "card_user", cardUser);

        String antId = (String)params.get("antId");
        wrapper.eq(StringUtils.isNotBlank(antId), "ant_id", antId);

        String agentId = (String)params.get("agentId");
        wrapper.eq(StringUtils.isNotBlank(agentId), "agent_id", agentId);

        String middleId = (String)params.get("middleId");
        if(StringUtils.isNotBlank(middleId)) {
            wrapper.eq("middle_id", Long.parseLong(middleId));
        }

        String userId = (String)params.get("userId");
        wrapper.eq(StringUtils.isNotBlank(userId), "user_id", userId);

        String cardId = (String) params.get("cardId");
        if (StringUtils.isNotBlank(cardId)) {
            wrapper.eq("card_id", Long.parseLong(cardId));
        }

        String startDate = (String) params.get("startDate");
        if (StringUtils.isNotBlank((String) params.get("startDate"))) {
            wrapper.ge("create_date", startDate);
        }
        String endDate = (String) params.get("endDate");
        if (StringUtils.isNotBlank((String) params.get("endDate"))) {
            wrapper.le("create_date", endDate);
        }

        return wrapper;
    }


}