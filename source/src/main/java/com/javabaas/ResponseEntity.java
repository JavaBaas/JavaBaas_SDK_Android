package com.javabaas;

import java.util.Map;

/**
 * Created by xueshukai on 15/9/24 下午2:43.
 */
public class ResponseEntity {
    private int code;
    private String message;
    private Map<String,Object> data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

}
