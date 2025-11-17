package io.renren.bledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.bledger.dao.BotLogDao;
import io.renren.bledger.dto.BotLogDTO;
import io.renren.bledger.entity.BotLogEntity;
import io.renren.bledger.service.BotLogService;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 余额流水
 *
 * @author kernel kernel@qq.com
 * @since 1.0 2024-06-02
 */
@Service
public class BotLogServiceImpl extends CrudServiceImpl<BotLogDao, BotLogEntity, BotLogDTO> implements BotLogService {

    @DataFilter(userId = "user_id")
    @Override
    public PageData<BotLogDTO> page(Map<String, Object> params) {
        IPage<BotLogEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, BotLogDTO.class);
    }
    @Override
    public QueryWrapper<BotLogEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<BotLogEntity> wrapper = new QueryWrapper<>();

        String userName = (String)params.get("userName");
        if(userName != null && StringUtils.isNotBlank(userName)) {
            wrapper.likeRight("user_name", userName);
        }
        return wrapper;
    }
}
