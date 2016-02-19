package com.javabaas.callback;

import com.javabaas.exception.JBException;

/**
 * Created by xueshukai on 16/1/26 下午3:08.
 */
public interface RequestCallback {
    void done();
    void error(JBException e);
}
