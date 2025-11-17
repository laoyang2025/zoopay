package io.renren.bledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import io.renren.bledger.dao.BotAccountDao;
import io.renren.bledger.dto.BotAccountDTO;
import io.renren.bledger.entity.BotAccountEntity;
import io.renren.bledger.service.BotAccountService;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 机器人账号
 *
 * @author kernel kernel@qq.com
 * @since 1.0 2024-06-02
 */
@Service
public class BotAccountServiceImpl extends CrudServiceImpl<BotAccountDao, BotAccountEntity, BotAccountDTO> implements BotAccountService {

    @DataFilter(userId = "user_id")
    @Override
    public PageData<BotAccountDTO> page(Map<String, Object> params) {
        IPage<BotAccountEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, BotAccountDTO.class);
    }

    @Override
    public QueryWrapper<BotAccountEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<BotAccountEntity> wrapper = new QueryWrapper<>();

        String userName = (String)params.get("userName");
        if(userName != null && StringUtils.isNotBlank(userName)) {
            wrapper.likeRight("user_name", userName);
        }
        return wrapper;
    }
}
