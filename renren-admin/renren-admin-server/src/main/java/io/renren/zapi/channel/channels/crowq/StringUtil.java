package io.renren.zapi.channel.channels.crowq;


import java.util.Map;
import java.util.TreeMap;



public class StringUtil {


    public static String buildSignSrc(Map params) {

        TreeMap<String, String> tempMap = new TreeMap<String, String>();
        for (Object key1 : params.keySet()) {
            String key=(String)key1;
            // 空参数不参与签名的参数
            String value = params.get(key).toString();
            if ("".equals(value) || value == null){
                continue;
            }
            tempMap.put(key, value);
        }

        StringBuilder buf = new StringBuilder();
        for (String key : tempMap.keySet()) {
            buf.append(key).append("=").append(tempMap.get(key)).append("&");
        }
        String src="";
        if (buf.toString().length() > 0){
            src=buf.substring(0, buf.length() - 1);
        }
        return src;
    }


}
