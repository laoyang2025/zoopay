package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.zadmin.dao.VhMerchantDao;
import io.renren.zadmin.dto.VdChargeChannelDTO;
import io.renren.zadmin.dto.VhMerchantDTO;
import io.renren.zadmin.dto.VhMerchantDTO;
import io.renren.zadmin.entity.VhMerchantEntity;
import io.renren.zadmin.entity.VhMerchantEntity;
import io.renren.zadmin.service.VhMerchantService;
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
public class VhMerchantServiceImpl extends CrudServiceImpl<VhMerchantDao, VhMerchantEntity, VhMerchantDTO> implements VhMerchantService {

    @DataFilter(userId = "")
    @Override
    public PageData<VhMerchantDTO> page(Map<String, Object> params) {
        IPage<VhMerchantEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, VhMerchantDTO.class);
    }

    @DataFilter(userId = "")
    @Override
    public List<VhMerchantDTO> list(Map<String, Object> params) {
        return super.list(params);
    }

    @Override
    public QueryWrapper<VhMerchantEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<VhMerchantEntity> wrapper = new QueryWrapper<>();

        String merchantName = (String)params.get("merchantName");
        wrapper.eq(StringUtils.isNotBlank(merchantName), "merchant_name", merchantName);

        return wrapper;
    }


}