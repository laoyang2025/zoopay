package io.renren.tenant;

import io.renren.dao.SysTenantDao;
import io.renren.dao.SysTenantDataSourceDao;
import io.renren.tenant.redis.SysTenantRedis;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "renren.tenant", value = "enable", havingValue = "true")
public class InitTenantConfig {
    @Resource
    private SysTenantDataSourceDao sysTenantDataSourceDao;
    @Resource
    private SysTenantDao sysTenantDao;
    @Resource
    private SysTenantRedis sysTenantRedis;

    @Bean
    public InitTenantService initTenantService() {
        return new InitTenantService(sysTenantDataSourceDao, sysTenantDao, sysTenantRedis);
    }

}
