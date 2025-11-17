package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.zadmin.dao.VdChargeCardDao;
import io.renren.zadmin.dto.VdChargeCardDTO;
import io.renren.zadmin.dto.VdChargeCardDTO;
import io.renren.zadmin.dto.VdChargeChannelDTO;
import io.renren.zadmin.entity.VdChargeCardEntity;
import io.renren.zadmin.entity.VdChargeCardEntity;
import io.renren.zadmin.service.VdChargeCardService;
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
public class VdChargeCardServiceImpl extends CrudServiceImpl<VdChargeCardDao, VdChargeCardEntity, VdChargeCardDTO> implements VdChargeCardService {

    @DataFilter(userId = "")
    @Override
    public PageData<VdChargeCardDTO> page(Map<String, Object> params) {
        IPage<VdChargeCardEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, VdChargeCardDTO.class);
    }

    @DataFilter(userId = "")
    @Override
    public List<VdChargeCardDTO> list(Map<String, Object> params) {
        return super.list(params);
    }

    @Override
    public QueryWrapper<VdChargeCardEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<VdChargeCardEntity> wrapper = new QueryWrapper<>();

        String createDate = (String)params.get("createDate");
        wrapper.eq(StringUtils.isNotBlank(createDate), "create_date", createDate);

        String merchantName = (String)params.get("merchantName");
        wrapper.eq(StringUtils.isNotBlank(merchantName), "merchant_name", merchantName);

        String cardUser = (String)params.get("cardUser");
        wrapper.eq(StringUtils.isNotBlank(cardUser), "card_user", cardUser);

        String cardNo = (String)params.get("cardNo");
        wrapper.eq(StringUtils.isNotBlank(cardNo), "card_no", cardNo);

        return wrapper;
    }


}