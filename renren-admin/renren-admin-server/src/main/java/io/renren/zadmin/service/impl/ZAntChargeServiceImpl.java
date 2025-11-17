package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.zadmin.dao.ZAntChargeDao;
import io.renren.zadmin.dto.ZAntChargeDTO;
import io.renren.zadmin.entity.ZAntChargeEntity;
import io.renren.zadmin.service.ZAntChargeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * z_charge
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Service
public class ZAntChargeServiceImpl extends CrudServiceImpl<ZAntChargeDao, ZAntChargeEntity, ZAntChargeDTO> implements ZAntChargeService {

    @DataFilter
    @Override
    public PageData<ZAntChargeDTO> page(Map<String, Object> params) {
        IPage<ZAntChargeEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, ZAntChargeDTO.class);
    }


    @Override
    public QueryWrapper<ZAntChargeEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<ZAntChargeEntity> wrapper = new QueryWrapper<>();

        String userName = (String)params.get("userName");
        wrapper.eq(StringUtils.isNotBlank(userName), "user_name", userName);
        String assignType = (String)params.get("assignType");
        wrapper.eq(StringUtils.isNotBlank(assignType), "assign_type", assignType);
        String basketId = (String)params.get("basketId");
        wrapper.eq(StringUtils.isNotBlank(basketId), "basket_id", basketId);
        String withdrawId = (String)params.get("withdrawId");
        wrapper.eq(StringUtils.isNotBlank(withdrawId), "withdraw_id", withdrawId);
        String utr = (String)params.get("utr");
        wrapper.eq(StringUtils.isNotBlank(utr), "utr", utr);
        String processStatus = (String)params.get("processStatus");
        wrapper.eq(StringUtils.isNotBlank(processStatus), "process_status", processStatus);
        String settleFlag = (String)params.get("settleFlag");
        wrapper.eq(StringUtils.isNotBlank(settleFlag), "settle_flag", settleFlag);
        String createDate = (String)params.get("createDate");
        wrapper.eq(StringUtils.isNotBlank(createDate), "create_date", createDate);

        String antId = (String)params.get("antId");
        if(StringUtils.isNotBlank(antId)) {
            wrapper.eq("ant_id", Long.parseLong(antId));
        }
        return wrapper;
    }


}