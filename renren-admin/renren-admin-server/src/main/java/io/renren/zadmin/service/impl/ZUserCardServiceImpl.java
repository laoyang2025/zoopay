package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.zadmin.dao.ZUserCardDao;
import io.renren.zadmin.dto.ZUserCardDTO;
import io.renren.zadmin.entity.ZUserCardEntity;
import io.renren.zadmin.service.ZUserCardService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * z_user_card
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Service
public class ZUserCardServiceImpl extends CrudServiceImpl<ZUserCardDao, ZUserCardEntity, ZUserCardDTO> implements ZUserCardService {

    @DataFilter
    @Override
    public PageData<ZUserCardDTO> page(Map<String, Object> params) {
        IPage<ZUserCardEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, ZUserCardDTO.class);
    }

    @Override
    public QueryWrapper<ZUserCardEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<ZUserCardEntity> wrapper = new QueryWrapper<>();

        String agentId = (String)params.get("agentId");
        wrapper.eq(StringUtils.isNotBlank(agentId), "agent_id", agentId);
        String userId = (String)params.get("userId");
        wrapper.eq(StringUtils.isNotBlank(userId), "user_id", userId);
        String enabled = (String)params.get("enabled");
        wrapper.eq(StringUtils.isNotBlank(enabled), "enabled", enabled);
        String accountUser = (String)params.get("accountUser");
        wrapper.eq(StringUtils.isNotBlank(accountUser), "account_user", accountUser);
        String accountNo = (String)params.get("accountNo");
        wrapper.eq(StringUtils.isNotBlank(accountNo), "account_no", accountNo);

        return wrapper;
    }


}