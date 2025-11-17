package io.renren.service;

import io.renren.commons.mybatis.service.CrudService;
import io.renren.entity.SysTenantDataSourceEntity;
import io.renren.tenant.dto.SysTenantDataSourceDTO;

/**
 * 租户数据源
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface SysTenantDataSourceService extends CrudService<SysTenantDataSourceEntity, SysTenantDataSourceDTO> {

}