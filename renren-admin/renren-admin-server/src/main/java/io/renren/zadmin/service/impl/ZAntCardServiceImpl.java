package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.zadmin.dao.ZAntCardDao;
import io.renren.zadmin.dto.ZAntCardDTO;
import io.renren.zadmin.entity.ZAntCardEntity;
import io.renren.zadmin.service.ZAntCardService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * z_ant_card
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Service
public class ZAntCardServiceImpl extends CrudServiceImpl<ZAntCardDao, ZAntCardEntity, ZAntCardDTO> implements ZAntCardService {

    @DataFilter
    @Override
    public PageData<ZAntCardDTO> page(Map<String, Object> params) {
        IPage<ZAntCardEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, ZAntCardDTO.class);
    }

    @Override
    public QueryWrapper<ZAntCardEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<ZAntCardEntity> wrapper = new QueryWrapper<>();

        String username = (String)params.get("username");
        wrapper.eq(StringUtils.isNotBlank(username), "username", username);

        String accountNo = (String)params.get("accountNo");
        wrapper.eq(StringUtils.isNotBlank(accountNo), "account_no", accountNo);

        String antId = (String)params.get("antId");
        if(StringUtils.isNotBlank(antId)) {
            wrapper.eq("ant_id", Long.parseLong(antId));
        }

        return wrapper;
    }


}