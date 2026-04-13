package io.renren.zapi.channel.channels.sspay.utils;

import java.util.Locale;
import java.util.UUID;

/**
 * @author Justin Yi
 * @version 1.0
 * @classname UuidUtil
 * @description TODO
 * @date 2022/3/8 9:34
 **/
public class UUIDUtil {

    public static String getUUID(){
        return UUID.randomUUID().toString().replaceAll("-","").toUpperCase(Locale.ROOT);
    }
}
