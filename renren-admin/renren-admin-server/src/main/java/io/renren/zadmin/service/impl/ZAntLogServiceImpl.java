package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.zadmin.dao.ZAntLogDao;
import io.renren.zadmin.dto.ZAntLogDTO;
import io.renren.zadmin.entity.ZAntLogEntity;
import io.renren.zadmin.service.ZAntLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * z_ant_log
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Service
public class ZAntLogServiceImpl extends CrudServiceImpl<ZAntLogDao, ZAntLogEntity, ZAntLogDTO> implements ZAntLogService {

    @DataFilter
    @Override
    public PageData<ZAntLogDTO> page(Map<String, Object> params) {
        IPage<ZAntLogEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, ZAntLogDTO.class);
    }

    @Override
    public QueryWrapper<ZAntLogEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<ZAntLogEntity> wrapper = new QueryWrapper<>();

        String antName = (String)params.get("antName");
        wrapper.eq(StringUtils.isNotBlank(antName), "ant_name", antName);
        String cardId = (String)params.get("cardId");
        wrapper.eq(StringUtils.isNotBlank(cardId), "card_id", cardId);
        String cardNo = (String)params.get("cardNo");
        wrapper.eq(StringUtils.isNotBlank(cardNo), "card_no", cardNo);
        String utr = (String)params.get("utr");
        wrapper.eq(StringUtils.isNotBlank(utr), "utr", utr);
        String tn = (String)params.get("tn");
        wrapper.eq(StringUtils.isNotBlank(tn), "tn", tn);

        String antId = (String)params.get("antId");
        if(StringUtils.isNotBlank(antId)) {
            wrapper.eq("ant_id", Long.parseLong(antId));
        }

        return wrapper;
    }


}