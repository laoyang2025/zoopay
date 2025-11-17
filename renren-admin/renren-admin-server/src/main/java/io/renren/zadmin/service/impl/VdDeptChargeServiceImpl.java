package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.zadmin.dao.VdDeptChargeDao;
import io.renren.zadmin.dto.VdDeptChargeDTO;
import io.renren.zadmin.dto.VdDeptChargeDTO;
import io.renren.zadmin.entity.VdDeptChargeEntity;
import io.renren.zadmin.entity.VdDeptChargeEntity;
import io.renren.zadmin.service.VdDeptChargeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * VIEW
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-09-10
 */
@Service
public class VdDeptChargeServiceImpl extends CrudServiceImpl<VdDeptChargeDao, VdDeptChargeEntity, VdDeptChargeDTO> implements VdDeptChargeService {

    @DataFilter(userId = "")
    @Override
    public PageData<VdDeptChargeDTO> page(Map<String, Object> params) {
        IPage<VdDeptChargeEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, VdDeptChargeDTO.class);
    }

    @Override
    public QueryWrapper<VdDeptChargeEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<VdDeptChargeEntity> wrapper = new QueryWrapper<>();

        String createDate = (String)params.get("createDate");
        wrapper.eq(StringUtils.isNotBlank(createDate), "create_date", createDate);

        return wrapper;
    }


}