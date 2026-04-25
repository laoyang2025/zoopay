package io.renren.zapi.controller;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ResourceWebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 访问路径：/images/xxx.png → 映射到 resources/images/ 下的文件
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/images/");

        registry.addResourceHandler("/landing/**")
                .addResourceLocations("classpath:/landing/");

    }
}