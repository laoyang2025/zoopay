package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.commons.tools.utils.ConvertUtils;
import io.renren.zadmin.dao.ZCardDao;
import io.renren.zadmin.dao.ZRouteDao;
import io.renren.zadmin.dto.ZCardDTO;
import io.renren.zadmin.entity.ZCardEntity;
import io.renren.zadmin.entity.ZRouteEntity;
import io.renren.zadmin.service.ZCardService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * z_card
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Service
public class ZCardServiceImpl extends CrudServiceImpl<ZCardDao, ZCardEntity, ZCardDTO> implements ZCardService {

    @DataFilter
    @Override
    public PageData<ZCardDTO> page(Map<String, Object> params) {
        IPage<ZCardEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, ZCardDTO.class);
    }

    @DataFilter
    @Override
    public List<ZCardDTO> list(Map<String, Object> params) {
        List<ZCardEntity> list = baseDao.selectList(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return ConvertUtils.sourceToTarget(list, ZCardDTO.class);
    }


    @Override
    public QueryWrapper<ZCardEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<ZCardEntity> wrapper = new QueryWrapper<>();

        String chargeEnabled = (String)params.get("chargeEnabled");
        wrapper.eq(StringUtils.isNotBlank(chargeEnabled), "charge_enabled", chargeEnabled);
        String withdrawEnabled = (String)params.get("withdrawEnabled");
        wrapper.eq(StringUtils.isNotBlank(withdrawEnabled), "withdraw_enabled", withdrawEnabled);
        String accountUser = (String)params.get("accountUser");
        wrapper.eq(StringUtils.isNotBlank(accountUser), "account_user", accountUser);
        String accountNo = (String)params.get("accountNo");
        wrapper.eq(StringUtils.isNotBlank(accountNo), "account_no", accountNo);

        return wrapper;
    }

    @Resource
    private ZRouteDao zRouteDao;

    @Override
    public void delete(Long[] ids) {
        super.delete(ids);
        zRouteDao.delete(Wrappers.<ZRouteEntity>lambdaQuery()
                .in(ZRouteEntity::getObjectId, ids)
        );
    }
}