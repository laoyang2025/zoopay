package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.zadmin.dao.ZBasketDao;
import io.renren.zadmin.dto.ZBasketDTO;
import io.renren.zadmin.entity.ZBasketEntity;
import io.renren.zadmin.service.ZBasketService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * z_basket
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Service
public class ZBasketServiceImpl extends CrudServiceImpl<ZBasketDao, ZBasketEntity, ZBasketDTO> implements ZBasketService {

    @DataFilter
    @Override
    public PageData<ZBasketDTO> page(Map<String, Object> params) {
        IPage<ZBasketEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, ZBasketDTO.class);
    }

    @Override
    public QueryWrapper<ZBasketEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<ZBasketEntity> wrapper = new QueryWrapper<>();

        String accountUser = (String)params.get("accountUser");
        wrapper.eq(StringUtils.isNotBlank(accountUser), "account_user", accountUser);
        String accountNo = (String)params.get("accountNo");
        wrapper.eq(StringUtils.isNotBlank(accountNo), "account_no", accountNo);

        return wrapper;
    }


}