package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.zadmin.dao.ZUserWithdrawDao;
import io.renren.zadmin.dto.ZUserWithdrawDTO;
import io.renren.zadmin.entity.ZUserWithdrawEntity;
import io.renren.zadmin.service.ZUserWithdrawService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * z_user_withdraw
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Service
public class ZUserWithdrawServiceImpl extends CrudServiceImpl<ZUserWithdrawDao, ZUserWithdrawEntity, ZUserWithdrawDTO> implements ZUserWithdrawService {

    @DataFilter
    @Override
    public PageData<ZUserWithdrawDTO> page(Map<String, Object> params) {
        IPage<ZUserWithdrawEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, ZUserWithdrawDTO.class);
    }

    @Override
    public QueryWrapper<ZUserWithdrawEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<ZUserWithdrawEntity> wrapper = new QueryWrapper<>();

        String agentId = (String)params.get("agentId");
        wrapper.eq(StringUtils.isNotBlank(agentId), "agent_id", agentId);
        String userId = (String)params.get("userId");
        wrapper.eq(StringUtils.isNotBlank(userId), "user_id", userId);
        String basketId = (String)params.get("basketId");
        wrapper.eq(StringUtils.isNotBlank(basketId), "basket_id", basketId);
        String accountUser = (String)params.get("accountUser");
        wrapper.eq(StringUtils.isNotBlank(accountUser), "account_user", accountUser);
        String accountNo = (String)params.get("accountNo");
        wrapper.eq(StringUtils.isNotBlank(accountNo), "account_no", accountNo);
        String processStatus = (String)params.get("processStatus");
        wrapper.eq(StringUtils.isNotBlank(processStatus), "process_status", processStatus);
        String utr = (String)params.get("utr");
        wrapper.eq(StringUtils.isNotBlank(utr), "utr", utr);

        return wrapper;
    }


}