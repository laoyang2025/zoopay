package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.zadmin.dao.ZBalanceDao;
import io.renren.zadmin.dto.ZBalanceDTO;
import io.renren.zadmin.entity.ZBalanceEntity;
import io.renren.zadmin.service.ZBalanceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * z_withdraw
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Service
public class ZBalanceServiceImpl extends CrudServiceImpl<ZBalanceDao, ZBalanceEntity, ZBalanceDTO> implements ZBalanceService {

    @DataFilter
    @Override
    public PageData<ZBalanceDTO> page(Map<String, Object> params) {
        IPage<ZBalanceEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, ZBalanceDTO.class);
    }

    @Override
    public QueryWrapper<ZBalanceEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<ZBalanceEntity> wrapper = new QueryWrapper<>();

        String parentId = (String)params.get("parentId");
        wrapper.eq(StringUtils.isNotBlank(parentId), "parent_id", parentId);
        String ownerType = (String)params.get("ownerType");
        wrapper.eq(StringUtils.isNotBlank(ownerType), "owner_type", ownerType);
        String ownerId = (String)params.get("ownerId");
        wrapper.eq(StringUtils.isNotBlank(ownerId), "owner_id", ownerId);

        return wrapper;
    }


}