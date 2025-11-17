package io.renren.bledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import io.renren.bledger.dao.BotChargeDao;
import io.renren.bledger.dto.BotChargeDTO;
import io.renren.bledger.entity.BotChargeEntity;
import io.renren.bledger.service.BotChargeService;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 充值
 *
 * @author kernel kernel@qq.com
 * @since 1.0 2024-06-02
 */
@Service
public class BotChargeServiceImpl extends CrudServiceImpl<BotChargeDao, BotChargeEntity, BotChargeDTO> implements BotChargeService {

    @DataFilter(userId = "user_id")
    @Override
    public PageData<BotChargeDTO> page(Map<String, Object> params) {
        IPage<BotChargeEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, BotChargeDTO.class);
    }

    @Override
    public QueryWrapper<BotChargeEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<BotChargeEntity> wrapper = new QueryWrapper<>();

        String userName = (String)params.get("userName");
        if(userName != null && StringUtils.isNotBlank(userName)) {
            wrapper.likeRight("user_name", userName);
        }
        return wrapper;
    }

}
