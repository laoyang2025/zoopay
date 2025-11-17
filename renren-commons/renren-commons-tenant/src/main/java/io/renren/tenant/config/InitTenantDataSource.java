/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.tenant.config;

import cn.hutool.core.map.MapUtil;
import com.alibaba.druid.pool.DruidDataSource;
import io.renren.commons.dynamic.datasource.config.DynamicDataSource;
import io.renren.commons.dynamic.datasource.config.DynamicDataSourceFactory;
import io.renren.commons.dynamic.datasource.properties.DataSourceProperties;
import io.renren.tenant.dto.SysTenantDataSourceDTO;
import io.renren.tenant.redis.SysTenantRedis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 初始化租户数据源
 *
 * @author Mark sunlightcs@gmail.com
 */
public class InitTenantDataSource implements CommandLineRunner {
    @Autowired
    private SysTenantRedis sysTenantRedis;
    @Autowired
    private DynamicDataSource dynamicDataSource;

    @Override
    public void run(String... args) {
        List<SysTenantDataSourceDTO> dataSourcePropertiesList = sysTenantRedis.getDatasourceCache();

        Map<Object, Object> dataSources = new HashMap<>();
        for (SysTenantDataSourceDTO tenantDataSource : dataSourcePropertiesList) {
            DataSourceProperties dataSourceProperties = new DataSourceProperties();
            dataSourceProperties.setDriverClassName(tenantDataSource.getDriverClassName());
            dataSourceProperties.setUrl(tenantDataSource.getUrl());
            dataSourceProperties.setUsername(tenantDataSource.getUsername());
            dataSourceProperties.setPassword(tenantDataSource.getPassword());
            DruidDataSource druidDataSource = DynamicDataSourceFactory.buildDruidDataSource(dataSourceProperties);

            dataSources.put(tenantDataSource.getId() + "", druidDataSource);
        }

        if (MapUtil.isNotEmpty(dataSources)) {
            dynamicDataSource.addDataSources(dataSources);
        }
    }
}