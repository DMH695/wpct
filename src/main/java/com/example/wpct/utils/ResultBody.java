package com.example.wpct.utils;


public class ResultBody {
    private boolean success;
    private int code;
    private Object body;
    public ResultBody(boolean success, int code, Object body){
        this.success = success;
        this.code = code;
        this.body = body;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getCode() {
        return code;
    }

    public Object getBody() {
        return body;
    }

    public static ResultBody ok(Object body){
        return new ResultBody(true,200,body);
    }

}
