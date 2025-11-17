package io.renren.zapi.merchant.dto;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    public static Result ok = new Result();
    private int code = 0;
    private String msg = "success";
    private T data;

    public static Result fail(int code, String msg) {
        Result rtn = new Result();
        rtn.setMsg(msg);
        rtn.setCode(code);
        return rtn;
    }

    public Result<T> ok(T data) {
        this.setData(data);
        return this;
    }

    public boolean success() {
        return code == 0;
    }
}
