package io.renren.dao;

import io.renren.commons.mybatis.dao.BaseDao;
import io.renren.entity.SysTenantDataSourceEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户数据源
 *
 * @author Mark sunlightcs@gmail.com
 */
@Mapper
public interface SysTenantDataSourceDao extends BaseDao<SysTenantDataSourceEntity> {

}