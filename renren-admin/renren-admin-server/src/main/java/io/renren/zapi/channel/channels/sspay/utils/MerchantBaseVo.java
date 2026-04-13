package io.renren.zapi.channel.channels.sspay.utils;

import java.io.Serializable;

/**
 * @author Justin Yi
 * @version 1.0
 * @classname MerchantBaseVo
 * @description TODO
 * @date 2022/3/8 9:01
 **/
public class MerchantBaseVo implements Serializable {

    /**
     * 商户号
     */
    private String mno;

    /**
     * 内容
     * 根据商户 AES-128-ECB 的 key（密码）值（16 位字符），示例：
     * IWNkNNzDE8H0BA==
     * 将所有 content  请求参数进行 键名字母升序排序 后转为 json
     * 字符串，使用 AES-128-ECB 用 key（密码）加密为 BASE64  编
     * 码字符串
     * 【第三方在线生成可与本地调试生成对比】
     * AES-128-ECB  加密/ 解密：http://tool.chacuo.net/cryptaes
     */
    private String content;

    /**
     * 商户交易订单号（商户自定义生成， 若有包含字母必须大写）
     */
    private String orderno;

    public String getMno() {
        return mno;
    }

    public void setMno(String mno) {
        this.mno = mno;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getOrderno() {
        return orderno;
    }

    public void setOrderno(String orderno) {
        this.orderno = orderno;
    }
}
