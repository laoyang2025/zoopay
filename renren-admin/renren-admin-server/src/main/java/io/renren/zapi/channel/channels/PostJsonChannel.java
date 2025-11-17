package io.renren.zapi.channel.channels;

import java.util.TreeMap;

public abstract class PostJsonChannel extends AbstractChannel {

    /**
     * 每个API要如何请求
     */
    @Override
    public String request(String url, TreeMap<String, Object> map, String api) {
        return this.postJSON(url, map);
    }

}