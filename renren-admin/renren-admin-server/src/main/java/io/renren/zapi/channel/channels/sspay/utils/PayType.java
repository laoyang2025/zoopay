package io.renren.zapi.channel.channels.sspay.utils;

/**
 * @author Justin Yi
 * @version 1.0
 * @classname PayType
 * @description TODO
 * @date 2022/3/8 9:35
 **/
public enum PayType {


    ZFB(1,"支付宝"),
    WX(2,"微信"),
    WG(3,"网关"),
    YL(4,"银联"),
    BDQB(5,"百度钱包"),
    JDZF(6,"京东支付"),;

    PayType(int code, String value) {
        this.code = code;
        this.value = value;
    }

    private int code;

    private String value;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
