package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.zadmin.dao.ZSmsDao;
import io.renren.zadmin.dto.ZSmsDTO;
import io.renren.zadmin.entity.ZSmsEntity;
import io.renren.zadmin.service.ZSmsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * z_sms
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-09-10
 */
@Service
public class ZSmsServiceImpl extends CrudServiceImpl<ZSmsDao, ZSmsEntity, ZSmsDTO> implements ZSmsService {

    @Override
    public QueryWrapper<ZSmsEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<ZSmsEntity> wrapper = new QueryWrapper<>();

        String phone = (String)params.get("phone");
        wrapper.eq(StringUtils.isNotBlank(phone), "phone", phone);

        return wrapper;
    }


}