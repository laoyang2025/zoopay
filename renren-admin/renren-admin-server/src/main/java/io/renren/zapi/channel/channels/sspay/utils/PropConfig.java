package io.renren.zapi.channel.channels.sspay.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * @author Justin Yi
 * @version 1.0
 * @classname propConfig
 * @description TODO
 * @date 2022/3/8 10:44
 **/
@Configuration
public class PropConfig {

    @Value("${demo.create_order_url}")
    private String createorderUrl;

    @Value("${demo.query_order_url}")
    private String queryOrderUrl;

    public String getCreateorderUrl() {
        return createorderUrl;
    }

    public void setCreateorderUrl(String createorderUrl) {
        this.createorderUrl = createorderUrl;
    }

    public String getQueryOrderUrl() {
        return queryOrderUrl;
    }

    public void setQueryOrderUrl(String queryOrderUrl) {
        this.queryOrderUrl = queryOrderUrl;
    }
}
