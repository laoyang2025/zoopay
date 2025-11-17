package io.renren.tenant.config;

import cn.hutool.core.exceptions.UtilException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import io.renren.commons.dynamic.datasource.config.DynamicContextHolder;
import io.renren.commons.security.context.TenantContext;
import io.renren.commons.security.user.SecurityUser;
import io.renren.commons.tools.enums.TenantModeEnum;
import io.renren.commons.tools.utils.HttpContextUtils;
import io.renren.commons.tools.utils.JsonUtils;
import io.renren.commons.tools.utils.Result;
import io.renren.tenant.dto.SysTenantListDTO;
import io.renren.tenant.redis.SysTenantRedis;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Objects;

/**
 * 租户 数据源切换
 *
 * @author Mark sunlightcs@gmail.com
 */
@Component
public class TenantRequestFilter extends OncePerRequestFilter {
    @Resource
    private TenantProperties tenantProperties;
    @Resource
    private PathMatcher pathMatcher;
    @Resource
    private SysTenantRedis sysTenantRedis;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (request.getMethod().equalsIgnoreCase(HttpMethod.OPTIONS.name())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isIgnoreUrl(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String domain = HttpContextUtils.getVisitDomain();
        final Long tenantCode = TenantContext.getTenantCode(SecurityUser.getUser());

        List<SysTenantListDTO> tenantList = sysTenantRedis.getCache();

        SysTenantListDTO sysTenant = tenantList.stream().filter(tenant -> Objects.equals(tenant.getTenantCode(), tenantCode)).findFirst().orElse(null);

        // 判断域名是否匹配
        for (SysTenantListDTO tenant : tenantList) {
            if (StrUtil.equalsIgnoreCase(tenant.getTenantDomain(), domain)) {
                sysTenant = tenant;
                break;
            }
        }

        // 租户不存在
        if (sysTenant == null) {
            Result<Object> result = new Result<>().error("租户编号：" + tenantCode + "，不存在，请联系管理员");
            writeJSON(request, response, result);
            return;
        }

        // 当前访问者租户编码
        TenantContext.setVisitorTenantCode(sysTenant.getTenantCode());

        // 切换数据源
        if (ObjectUtil.equals(sysTenant.getTenantMode(), TenantModeEnum.DATASOURCE.value())) {
            DynamicContextHolder.push(sysTenant.getDatasourceId() + "");
        }

        filterChain.doFilter(request, response);

        // 清除数据源
        if (ObjectUtil.equals(sysTenant.getTenantMode(), TenantModeEnum.DATASOURCE.value())) {
            DynamicContextHolder.poll();
        }

    }

    private boolean isIgnoreUrl(HttpServletRequest request) {
        for (String url : tenantProperties.getIgnoreUrls()) {
            if (pathMatcher.match(url, request.getServletPath())) {
                return true;
            }
        }
        return false;
    }

    private void writeJSON(HttpServletRequest request, HttpServletResponse response, Object object) {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Origin", request.getHeader(HttpHeaders.ORIGIN));

        String content = JsonUtils.toJsonString(object);
        Writer writer = null;
        try {
            writer = response.getWriter();
            writer.write(content);
            writer.flush();
        } catch (IOException e) {
            throw new UtilException(e);
        } finally {
            IoUtil.close(writer);
        }
    }

}
