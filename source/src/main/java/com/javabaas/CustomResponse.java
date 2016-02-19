package com.javabaas;

import java.util.List;

/**
 * Created by xueshukai on 15/12/18 下午2:53.
 */
public class CustomResponse {
    private String data;
    private int statusCode;
    private List<JBObject> queryResults;

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

    public List<JBObject> getQueryResults() {
        return queryResults;
    }

    public void setQueryResults(List<JBObject> queryResults) {
        this.queryResults = queryResults;
    }
}
