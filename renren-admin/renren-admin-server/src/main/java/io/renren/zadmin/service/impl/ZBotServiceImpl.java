package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.zadmin.dao.ZBotDao;
import io.renren.zadmin.dto.ZBotDTO;
import io.renren.zadmin.entity.ZBotEntity;
import io.renren.zadmin.service.ZBotService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 机器人账号
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-18
 */
@Service
public class ZBotServiceImpl extends CrudServiceImpl<ZBotDao, ZBotEntity, ZBotDTO> implements ZBotService {

    @Override
    public QueryWrapper<ZBotEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<ZBotEntity> wrapper = new QueryWrapper<>();


        return wrapper;
    }


}