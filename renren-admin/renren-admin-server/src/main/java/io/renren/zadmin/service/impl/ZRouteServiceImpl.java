package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.commons.tools.utils.ConvertUtils;
import io.renren.zadmin.dao.ZRouteDao;
import io.renren.zadmin.dto.ZRouteDTO;
import io.renren.zadmin.entity.ZRouteEntity;
import io.renren.zadmin.service.ZRouteService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * z_route
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-12
 */
@Service
public class ZRouteServiceImpl extends CrudServiceImpl<ZRouteDao, ZRouteEntity, ZRouteDTO> implements ZRouteService {

    @DataFilter
    @Override
    public PageData<ZRouteDTO> page(Map<String, Object> params) {
        IPage<ZRouteEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, ZRouteDTO.class);
    }

    @DataFilter
    @Override
    public List<ZRouteDTO> list(Map<String, Object> params) {
        List<ZRouteEntity> list = baseDao.selectList(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return ConvertUtils.sourceToTarget(list, ZRouteDTO.class);
    }


    @Override
    public QueryWrapper<ZRouteEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<ZRouteEntity> wrapper = new QueryWrapper<>();

        // 商户名
        String merchantName = (String)params.get("merchantName");
        wrapper.like(StringUtils.isNotBlank(merchantName), "merchant_name", merchantName);

        // 商户id
        String merchantId = (String)params.get("merchantId");
        if (StringUtils.isNotBlank(merchantId)) {
            wrapper.like("merchant_id", Long.parseLong(merchantId));
        }

        // 处理模式
        String processMode = (String)params.get("processMode");
        wrapper.eq(StringUtils.isNotBlank(processMode), "process_mode", processMode);

        // 收款启用
        String enabled = (String)params.get("enabled");
        if(StringUtils.isNotBlank(enabled)) {
            wrapper.eq("enabled", Integer.parseInt(enabled));
        }

        // 代付|收款
        String routeType = (String)params.get("routeType");
        if(StringUtils.isNotBlank(routeType)) {
            wrapper.eq("route_type", routeType);
        }

        return wrapper;
    }


}