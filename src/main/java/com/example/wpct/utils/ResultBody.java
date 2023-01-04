package com.example.wpct.utils;


public class ResultBody<T> {
    private boolean success;
    private int code;
    private T body;
    public ResultBody(boolean success, int code, T body){
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

    public void setBody(T body) {
        this.body = body;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getCode() {
        return code;
    }

    public T getBody() {
        return body;
    }

}