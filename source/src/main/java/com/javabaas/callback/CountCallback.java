package com.javabaas.callback;


import com.javabaas.exception.JBException;

/**
 * Created by xueshukai on 15/10/26 下午3:40.
 */
public interface CountCallback {
    void done(int count);
    void error(JBException e);
}
