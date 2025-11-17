package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.zadmin.dao.VdDeptWithdrawDao;
import io.renren.zadmin.dto.VdDeptChargeDTO;
import io.renren.zadmin.dto.VdDeptWithdrawDTO;
import io.renren.zadmin.dto.VdDeptWithdrawDTO;
import io.renren.zadmin.entity.VdDeptWithdrawEntity;
import io.renren.zadmin.entity.VdDeptWithdrawEntity;
import io.renren.zadmin.service.VdDeptWithdrawService;
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
public class VdDeptWithdrawServiceImpl extends CrudServiceImpl<VdDeptWithdrawDao, VdDeptWithdrawEntity, VdDeptWithdrawDTO> implements VdDeptWithdrawService {

    @DataFilter(userId =  "")
    @Override
    public PageData<VdDeptWithdrawDTO> page(Map<String, Object> params) {
        IPage<VdDeptWithdrawEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, VdDeptWithdrawDTO.class);
    }

    @DataFilter(userId = "")
    @Override
    public List<VdDeptWithdrawDTO> list(Map<String, Object> params) {
        return super.list(params);
    }

    @Override
    public QueryWrapper<VdDeptWithdrawEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<VdDeptWithdrawEntity> wrapper = new QueryWrapper<>();

        String createDate = (String)params.get("createDate");
        wrapper.eq(StringUtils.isNotBlank(createDate), "create_date", createDate);

        return wrapper;
    }


}