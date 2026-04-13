package io.renren.zapi.channel.channels.sspay.utils;

import java.io.Serializable;

import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;

/**
 * @author Justin Yi
 * @version 1.0
 * @classname Result
 * @description TODO
 * @date 2022/3/8 11:02
 **/
public class Result implements Serializable {


    private String code;
    private String msg;
    private String data;


    public Result() {
    }

    public Result(String code, String msg, String data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static  Result success() {
        Result result=new Result("success","成功","");
        return result;
    }

    public static  Result fail() {
        Result result=new Result("error","失败","");
        return result;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
