package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.zadmin.dao.ZWarningDao;
import io.renren.zadmin.dto.ZWarningDTO;
import io.renren.zadmin.entity.ZWarningEntity;
import io.renren.zadmin.service.ZWarningService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * z_warning
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-15
 */
@Service
public class ZWarningServiceImpl extends CrudServiceImpl<ZWarningDao, ZWarningEntity, ZWarningDTO> implements ZWarningService {


    @DataFilter
    @Override
    public PageData<ZWarningDTO> page(Map<String, Object> params) {
        IPage<ZWarningEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, ZWarningDTO.class);
    }

    @Override
    public QueryWrapper<ZWarningEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<ZWarningEntity> wrapper = new QueryWrapper<>();

        String msgType = (String)params.get("msgType");
        wrapper.eq(StringUtils.isNotBlank(msgType), "msg_type", msgType);

        String msg = (String)params.get("msg");
        wrapper.like(StringUtils.isNotBlank(msg), "msg", msg);

        return wrapper;
    }


}