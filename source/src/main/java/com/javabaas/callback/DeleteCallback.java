package com.javabaas.callback;


import com.javabaas.exception.JBException;

/**
 * Created by xueshukai on 15/10/8 下午3:40.
 */
public interface DeleteCallback {
    void done();
    void error(JBException e);
}
