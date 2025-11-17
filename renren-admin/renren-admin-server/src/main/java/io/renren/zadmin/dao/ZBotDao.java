package io.renren.zadmin.dao;

import io.renren.commons.mybatis.dao.BaseDao;
import io.renren.zadmin.entity.ZBotEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* 机器人账号
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-18
*/
@Mapper
public interface ZBotDao extends BaseDao<ZBotEntity> {
	
}