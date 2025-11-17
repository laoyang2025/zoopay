package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.zadmin.dao.VhChargeCardDao;
import io.renren.zadmin.dto.VdChargeChannelDTO;
import io.renren.zadmin.dto.VhChargeCardDTO;
import io.renren.zadmin.dto.VhChargeCardDTO;
import io.renren.zadmin.entity.VhChargeCardEntity;
import io.renren.zadmin.entity.VhChargeCardEntity;
import io.renren.zadmin.service.VhChargeCardService;
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
public class VhChargeCardServiceImpl extends CrudServiceImpl<VhChargeCardDao, VhChargeCardEntity, VhChargeCardDTO> implements VhChargeCardService {

    @DataFilter(userId = "")
    @Override
    public PageData<VhChargeCardDTO> page(Map<String, Object> params) {
        IPage<VhChargeCardEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, VhChargeCardDTO.class);
    }

    @DataFilter(userId = "")
    @Override
    public List<VhChargeCardDTO> list(Map<String, Object> params) {
        return super.list(params);
    }

    @Override
    public QueryWrapper<VhChargeCardEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<VhChargeCardEntity> wrapper = new QueryWrapper<>();

        String merchantName = (String)params.get("merchantName");
        wrapper.eq(StringUtils.isNotBlank(merchantName), "merchant_name", merchantName);
        String cardUser = (String)params.get("cardUser");
        wrapper.eq(StringUtils.isNotBlank(cardUser), "card_user", cardUser);

        return wrapper;
    }


}