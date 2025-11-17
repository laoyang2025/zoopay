package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.zadmin.dao.VdChannelDao;
import io.renren.zadmin.dto.VdChannelDTO;
import io.renren.zadmin.dto.VdChannelDTO;
import io.renren.zadmin.dto.VdChargeChannelDTO;
import io.renren.zadmin.entity.VdChannelEntity;
import io.renren.zadmin.entity.VdChannelEntity;
import io.renren.zadmin.service.VdChannelService;
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
public class VdChannelServiceImpl extends CrudServiceImpl<VdChannelDao, VdChannelEntity, VdChannelDTO> implements VdChannelService {

    @DataFilter(userId = "")
    @Override
    public PageData<VdChannelDTO> page(Map<String, Object> params) {
        IPage<VdChannelEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, VdChannelDTO.class);
    }

    @DataFilter(userId = "")
    @Override
    public List<VdChannelDTO> list(Map<String, Object> params) {
        return super.list(params);
    }


    @Override
    public QueryWrapper<VdChannelEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<VdChannelEntity> wrapper = new QueryWrapper<>();

        String channelLabel = (String)params.get("channelLabel");
        wrapper.eq(StringUtils.isNotBlank(channelLabel), "channel_label", channelLabel);

        String createDate = (String)params.get("createDate");
        wrapper.eq(StringUtils.isNotBlank(createDate), "create_date", createDate);
        return wrapper;
    }


}