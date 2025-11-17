package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.commons.tools.utils.ConvertUtils;
import io.renren.zadmin.dao.ZChannelDao;
import io.renren.zadmin.dto.ZChannelDTO;
import io.renren.zadmin.entity.ZChannelEntity;
import io.renren.zadmin.service.ZChannelService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * z_channel
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Service
public class ZChannelServiceImpl extends CrudServiceImpl<ZChannelDao, ZChannelEntity, ZChannelDTO> implements ZChannelService {

    @DataFilter
    @Override
    public PageData<ZChannelDTO> page(Map<String, Object> params) {
        IPage<ZChannelEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, ZChannelDTO.class);
    }

    @DataFilter
    @Override
    public List<ZChannelDTO> list(Map<String, Object> params) {
        List<ZChannelEntity> list = baseDao.selectList(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return ConvertUtils.sourceToTarget(list, ZChannelDTO.class);
    }

    @Override
    public QueryWrapper<ZChannelEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<ZChannelEntity> wrapper = new QueryWrapper<>();


        return wrapper;
    }


}