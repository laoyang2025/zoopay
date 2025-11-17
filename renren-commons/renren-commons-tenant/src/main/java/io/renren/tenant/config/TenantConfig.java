package io.renren.tenant.config;

import jakarta.annotation.Resource;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "renren.tenant", value = "enable", havingValue = "true")
@EnableConfigurationProperties(TenantProperties.class)
public class TenantConfig {
    @Resource
    private TenantRequestFilter tenantRequestFilter;
    
    @Bean
    public InitTenantDataSource tenantInterceptor() {
        return new InitTenantDataSource();
    }

    @Bean
    public FilterRegistrationBean<TenantRequestFilter> tenantContextWebFilter() {
        FilterRegistrationBean<TenantRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(tenantRequestFilter);
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.addUrlPatterns("/*");
        registration.setName("tenantFilter");
        registration.setOrder(0);
        return registration;
    }
}
