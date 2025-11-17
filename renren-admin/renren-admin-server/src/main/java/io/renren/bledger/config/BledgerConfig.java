package io.renren.bledger.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


// 只跑在一个机构下
@Configuration
@Data
@Slf4j
@ConfigurationProperties(prefix = "bledger")
public class BledgerConfig {
    private boolean debug;
    private boolean dev;
    private String botKey;
    private Long deptId;
    private String botName;
}
