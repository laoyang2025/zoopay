package io.renren.zadmin.dao;

import io.renren.commons.mybatis.dao.BaseDao;
import io.renren.zadmin.entity.ZLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* z_log
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@Mapper
public interface ZLogDao extends BaseDao<ZLogEntity> {
	
}