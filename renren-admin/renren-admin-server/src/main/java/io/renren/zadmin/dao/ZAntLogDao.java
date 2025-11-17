package io.renren.zadmin.dao;

import io.renren.commons.mybatis.dao.BaseDao;
import io.renren.zadmin.entity.ZAntLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* z_ant_log
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@Mapper
public interface ZAntLogDao extends BaseDao<ZAntLogEntity> {
	
}