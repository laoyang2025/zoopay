package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.zadmin.dao.ZUserLogDao;
import io.renren.zadmin.dto.ZUserLogDTO;
import io.renren.zadmin.entity.ZUserLogEntity;
import io.renren.zadmin.service.ZUserLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * z_user_log
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Service
public class ZUserLogServiceImpl extends CrudServiceImpl<ZUserLogDao, ZUserLogEntity, ZUserLogDTO> implements ZUserLogService {

    @DataFilter
    @Override
    public PageData<ZUserLogDTO> page(Map<String, Object> params) {
        IPage<ZUserLogEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, ZUserLogDTO.class);
    }

    @Override
    public QueryWrapper<ZUserLogEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<ZUserLogEntity> wrapper = new QueryWrapper<>();

        String agentId = (String)params.get("agentId");
        wrapper.eq(StringUtils.isNotBlank(agentId), "agent_id", agentId);
        String userId = (String)params.get("userId");
        wrapper.eq(StringUtils.isNotBlank(userId), "user_id", userId);
        String cardId = (String)params.get("cardId");
        wrapper.eq(StringUtils.isNotBlank(cardId), "card_id", cardId);
        String cardUser = (String)params.get("cardUser");
        wrapper.eq(StringUtils.isNotBlank(cardUser), "card_user", cardUser);
        String utr = (String)params.get("utr");
        wrapper.eq(StringUtils.isNotBlank(utr), "utr", utr);
        String tn = (String)params.get("tn");
        wrapper.eq(StringUtils.isNotBlank(tn), "tn", tn);
        String chargeId = (String)params.get("chargeId");
        wrapper.eq(StringUtils.isNotBlank(chargeId), "charge_id", chargeId);

        return wrapper;
    }


}