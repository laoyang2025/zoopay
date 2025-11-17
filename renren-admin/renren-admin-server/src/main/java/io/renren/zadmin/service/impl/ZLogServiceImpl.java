package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.commons.tools.utils.ConvertUtils;
import io.renren.zadmin.dao.ZLogDao;
import io.renren.zadmin.dto.ZCardDTO;
import io.renren.zadmin.dto.ZLogDTO;
import io.renren.zadmin.entity.ZLogEntity;
import io.renren.zadmin.service.ZLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * z_log
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Service
public class ZLogServiceImpl extends CrudServiceImpl<ZLogDao, ZLogEntity, ZLogDTO> implements ZLogService {

    @DataFilter
    @Override
    public PageData<ZLogDTO> page(Map<String, Object> params) {
        IPage<ZLogEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, ZLogDTO.class);
    }

    @DataFilter
    @Override
    public List<ZLogDTO> list(Map<String, Object> params) {
        List<ZLogEntity> list = baseDao.selectList(applyFilter(params));
        return ConvertUtils.sourceToTarget(list, ZLogDTO.class);
    }

    @Override
    public QueryWrapper<ZLogEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<ZLogEntity> wrapper = new QueryWrapper<>();

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

        String factMemo = (String) params.get("factMemo");
        if (StringUtils.isNotBlank(factMemo)) {
            System.out.println("filter by factMemo");
            wrapper.like("fact_memo", factMemo);
        }

        String endDate = (String) params.get("endDate");
        if (StringUtils.isNotBlank((String) params.get("endDate"))) {
            wrapper.le("create_date", endDate);
        }


        return wrapper;
    }


}