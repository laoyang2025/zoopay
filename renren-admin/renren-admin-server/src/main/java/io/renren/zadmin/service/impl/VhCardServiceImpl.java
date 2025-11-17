package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.zadmin.dao.VhCardDao;
import io.renren.zadmin.dto.VdChargeChannelDTO;
import io.renren.zadmin.dto.VhCardDTO;
import io.renren.zadmin.dto.VhCardDTO;
import io.renren.zadmin.entity.VhCardEntity;
import io.renren.zadmin.entity.VhCardEntity;
import io.renren.zadmin.service.VhCardService;
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
public class VhCardServiceImpl extends CrudServiceImpl<VhCardDao, VhCardEntity, VhCardDTO> implements VhCardService {

    @DataFilter(userId = "")
    @Override
    public PageData<VhCardDTO> page(Map<String, Object> params) {
        IPage<VhCardEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, VhCardDTO.class);
    }

    @DataFilter(userId = "")
    @Override
    public List<VhCardDTO> list(Map<String, Object> params) {
        return super.list(params);
    }

    @Override
    public QueryWrapper<VhCardEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<VhCardEntity> wrapper = new QueryWrapper<>();

        String cardUser = (String)params.get("cardUser");
        wrapper.eq(StringUtils.isNotBlank(cardUser), "card_user", cardUser);

        return wrapper;
    }


}