package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.zadmin.dao.ZAgentWithdrawDao;
import io.renren.zadmin.dto.ZAgentWithdrawDTO;
import io.renren.zadmin.entity.ZAgentWithdrawEntity;
import io.renren.zadmin.service.ZAgentWithdrawService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * z_agent_withdraw
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Service
public class ZAgentWithdrawServiceImpl extends CrudServiceImpl<ZAgentWithdrawDao, ZAgentWithdrawEntity, ZAgentWithdrawDTO> implements ZAgentWithdrawService {

    @DataFilter
    @Override
    public PageData<ZAgentWithdrawDTO> page(Map<String, Object> params) {
        IPage<ZAgentWithdrawEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, ZAgentWithdrawDTO.class);
    }

    @Override
    public QueryWrapper<ZAgentWithdrawEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<ZAgentWithdrawEntity> wrapper = new QueryWrapper<>();

        String agentId = (String)params.get("agentId");
        wrapper.eq(StringUtils.isNotBlank(agentId), "agent_id", agentId);
        String basketId = (String)params.get("basketId");
        wrapper.eq(StringUtils.isNotBlank(basketId), "basket_id", basketId);
        String processStatus = (String)params.get("processStatus");
        wrapper.eq(StringUtils.isNotBlank(processStatus), "process_status", processStatus);
        String utr = (String)params.get("utr");
        wrapper.eq(StringUtils.isNotBlank(utr), "utr", utr);

        return wrapper;
    }


}