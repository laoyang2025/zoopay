package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.zadmin.dao.ZUserChargeDao;
import io.renren.zadmin.dto.ZUserChargeDTO;
import io.renren.zadmin.entity.ZUserChargeEntity;
import io.renren.zadmin.service.ZUserChargeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * z_user_charge
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Service
public class ZUserChargeServiceImpl extends CrudServiceImpl<ZUserChargeDao, ZUserChargeEntity, ZUserChargeDTO> implements ZUserChargeService {

    @DataFilter
    @Override
    public PageData<ZUserChargeDTO> page(Map<String, Object> params) {
        IPage<ZUserChargeEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, ZUserChargeDTO.class);
    }

    @Override
    public QueryWrapper<ZUserChargeEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<ZUserChargeEntity> wrapper = new QueryWrapper<>();

        String agentId = (String)params.get("agentId");
        wrapper.eq(StringUtils.isNotBlank(agentId), "agent_id", agentId);
        String userId = (String)params.get("userId");
        wrapper.eq(StringUtils.isNotBlank(userId), "user_id", userId);
        String processStatus = (String)params.get("processStatus");
        wrapper.eq(StringUtils.isNotBlank(processStatus), "process_status", processStatus);
        String utr = (String)params.get("utr");
        wrapper.eq(StringUtils.isNotBlank(utr), "utr", utr);

        return wrapper;
    }


}