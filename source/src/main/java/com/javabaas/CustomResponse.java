package com.javabaas;

/**
 * Created by xueshukai on 15/12/18 下午2:53.
 */
public class CustomResponse {
    private String data;
    private int statusCode;

    public CustomResponse(String data) {
        this.data = data;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getData() {
        return data;
    }
}
