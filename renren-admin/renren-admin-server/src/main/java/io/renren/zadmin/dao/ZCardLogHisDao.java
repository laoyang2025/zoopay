package io.renren.zadmin.dao;

import io.renren.commons.mybatis.dao.BaseDao;
import io.renren.zadmin.entity.ZCardLogEntity;
import io.renren.zadmin.entity.ZCardLogHisEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* z_card_log
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@Mapper
public interface ZCardLogHisDao extends BaseDao<ZCardLogHisEntity> {
	
}