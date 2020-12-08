package com.tjslzhkj.coupon.vo;

/**
 * Created by Administrator on 2020/3/16.
 */

import java.io.Serializable;

/**
 * 统一响应模型
 * **
 * Yuezejian  Created in 2020/11/4 下午10:46
 */
public class BaseResponse<T> implements Serializable{

    private Integer code;
    private String msg;
    private T data;


    public BaseResponse(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public BaseResponse(StatusCode statusCode) {
        this.code = statusCode.getCode();
        this.msg = statusCode.getMsg();
    }

    public BaseResponse(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}