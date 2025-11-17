package io.renren.zapi;

import io.renren.commons.security.user.SecurityUser;
import io.renren.zsocket.SocketAdmin;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Configuration
@Data
@Slf4j
@ConfigurationProperties(prefix = "zoo")
public class ZConfig {
    // 是否debug
    private boolean debug;
    private boolean dev;
    private String cdnUrl;
    private String ocrApi;
    private Integer maxNotifyCount;
    private Long initNotifyInterval;
    private String uploadDir;
    private Map<String, List<String>> pushbullets;

    // 系统本地时区
    private Integer tzMinutes;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    public String fileUrl(String fileName) {
        // 开发环境是java提供文件服务, 生产环境是nginx
        if (this.dev) {
            return SecurityUser.getUser().getApiDomain() + "/sys/zupload/files/" + fileName;
        } else {
            return SecurityUser.getUser().getApiDomain() + "/zupload/" + fileName;
        }
    }
}
