package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.zadmin.dao.ZLogDao;
import io.renren.zadmin.dao.ZLogHisDao;
import io.renren.zadmin.dto.ZLogDTO;
import io.renren.zadmin.dto.ZLogHisDTO;
import io.renren.zadmin.entity.ZLogEntity;
import io.renren.zadmin.entity.ZLogHisEntity;
import io.renren.zadmin.service.ZLogHisService;
import io.renren.zadmin.service.ZLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * z_log
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Service
public class ZLogHisServiceImpl extends CrudServiceImpl<ZLogHisDao, ZLogHisEntity, ZLogHisDTO> implements ZLogHisService {

    @DataFilter
    @Override
    public PageData<ZLogHisDTO> page(Map<String, Object> params) {
        IPage<ZLogHisEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, ZLogHisDTO.class);
    }

    @Override
    public QueryWrapper<ZLogHisEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<ZLogHisEntity> wrapper = new QueryWrapper<>();

        String ownerId = (String)params.get("ownerId");
        wrapper.eq(StringUtils.isNotBlank(ownerId), "owner_id", ownerId);
        String factId = (String)params.get("factId");
        wrapper.eq(StringUtils.isNotBlank(factId), "fact_id", factId);
        String factType = (String)params.get("factType");
        wrapper.eq(StringUtils.isNotBlank(factType), "fact_type", factType);

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