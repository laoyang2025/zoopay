package io.renren.bledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.bledger.dao.BotPayDao;
import io.renren.bledger.dto.BotPayDTO;
import io.renren.bledger.entity.BotPayEntity;
import io.renren.bledger.service.BotPayService;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 付款
 *
 * @author kernel kernel@qq.com
 * @since 1.0 2024-06-02
 */
@Service
public class BotPayServiceImpl extends CrudServiceImpl<BotPayDao, BotPayEntity, BotPayDTO> implements BotPayService {

    @DataFilter(userId = "user_id")
    @Override
    public PageData<BotPayDTO> page(Map<String, Object> params) {
        QueryWrapper<BotPayEntity> wrapper = applyFilter(params);
        IPage<BotPayEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                wrapper
        );
        return getPageData(page, BotPayDTO.class);
    }

    @Override
    public QueryWrapper<BotPayEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<BotPayEntity> wrapper = new QueryWrapper<>();

        String userName = (String)params.get("userName");
        if(userName != null && StringUtils.isNotBlank(userName)) {
            wrapper.likeRight("user_name", userName);
        }

        return wrapper;
    }
}
