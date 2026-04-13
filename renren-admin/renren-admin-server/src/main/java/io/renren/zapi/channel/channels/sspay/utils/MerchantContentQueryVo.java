package io.renren.zapi.channel.channels.sspay.utils;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONType;

/**
 * @author Justin Yi
 * @version 1.0
 * @classname MerchantContentVo
 * @description TODO
 * @date 2022/3/8 9:02
 **/
@JSONType(orders={"amount","mno","orderno","sign"})
public class MerchantContentQueryVo implements Serializable {

    /**
     * 交易金额（单位：分）10.08 元=1008
     */
    private int amount;


    /**
     * 商户号
     */
    private String mno;

    /**
     * 商户交易订单号（商户自定义生成， 若有包含字母必须大写）
     */
    private String orderno;



    /**
     * 签名验证
     * 签名字段进行 键名字母 升序 排序 进行拼接字符串 ， 最后拼接
     * &key=MD5  密钥使用 MD5 加密
     * 具体生成签名请参考 DEMO
     * 签名字段示例：
     * amount=200000&async_notify_url=https://www.domain.com/p
     * ay/async_notify_url.php&mno=A200825005001688&orderno=2
     * 022030211405797597592090171&code=1&key=D9S
     * giVslrj1V1YK4L9JuBGqrpJhaMqJw
     * 使用 MD5 生成 sign（32 位小写）
     * 【第三方在线生成可与本地调试生成对比】
     * MD5  加密：https://tool.chinaz.com/Tools/MD5.aspx
     */
    private String sign;


    public String getMno() {
        return mno;
    }

    public void setMno(String mno) {
        this.mno = mno;
    }

    public String getOrderno() {
        return orderno;
    }

    public void setOrderno(String orderno) {
        this.orderno = orderno;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }


    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Override
    public String toString() {
        return "MerchantContentVo{" +
                "mno='" + mno + '\'' +
                ", orderno='" + orderno + '\'' +
                ", amount=" + amount +
                ", sign='" + sign + '\'' +
                '}';
    }
}
