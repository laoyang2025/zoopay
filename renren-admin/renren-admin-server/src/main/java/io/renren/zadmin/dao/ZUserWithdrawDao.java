package io.renren.zadmin.dao;

import io.renren.commons.mybatis.dao.BaseDao;
import io.renren.zadmin.entity.ZUserWithdrawEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* z_user_withdraw
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@Mapper
public interface ZUserWithdrawDao extends BaseDao<ZUserWithdrawEntity> {
	
}