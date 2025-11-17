package io.renren.zapi.channel.channels;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.TreeMap;

@Slf4j
public abstract class PostFormChannel extends AbstractChannel {

    /**
     * 每个API要如何请求
     * @return
     */
    @Override
    public String request(String url, TreeMap<String, Object> map, String api) {
        return this.postForm(url, map, null);
    }
}
