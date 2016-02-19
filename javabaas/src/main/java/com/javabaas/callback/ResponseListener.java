package com.javabaas.callback;


import com.javabaas.exception.JBException;

/**
 * Created by xueshukai on 15/10/9 下午5:49.
 */
public interface ResponseListener<T> {
    void onResponse(T entity);

    void onError(JBException e);
}
