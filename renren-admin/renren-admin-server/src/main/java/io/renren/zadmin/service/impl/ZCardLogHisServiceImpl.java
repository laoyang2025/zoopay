package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.zadmin.dao.ZCardLogDao;
import io.renren.zadmin.dao.ZCardLogHisDao;
import io.renren.zadmin.dto.ZCardLogDTO;
import io.renren.zadmin.dto.ZCardLogHisDTO;
import io.renren.zadmin.entity.ZCardLogEntity;
import io.renren.zadmin.entity.ZCardLogHisEntity;
import io.renren.zadmin.service.ZCardLogHisService;
import io.renren.zadmin.service.ZCardLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * z_card_log
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Service
public class ZCardLogHisServiceImpl extends CrudServiceImpl<ZCardLogHisDao, ZCardLogHisEntity, ZCardLogHisDTO> implements ZCardLogHisService {

    @DataFilter
    @Override
    public PageData<ZCardLogHisDTO> page(Map<String, Object> params) {
        IPage<ZCardLogHisEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, ZCardLogHisDTO.class);
    }

    @Override
    public QueryWrapper<ZCardLogHisEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<ZCardLogHisEntity> wrapper = new QueryWrapper<>();

        String cardId = (String)params.get("cardId");
        if (StringUtils.isNotBlank(cardId)) {
            wrapper.eq("card_id", Long.parseLong(cardId));
        }
        String utr = (String)params.get("utr");
        wrapper.eq(StringUtils.isNotBlank(utr), "utr", utr);

        String tn = (String)params.get("tn");
        wrapper.eq(StringUtils.isNotBlank(tn), "tn", tn);

        String chargeId = (String)params.get("chargeId");
        wrapper.eq(StringUtils.isNotBlank(chargeId), "charge_id", chargeId);

        return wrapper;
    }


}