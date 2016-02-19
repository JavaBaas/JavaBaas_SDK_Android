package com.javabaas.callback;

import com.javabaas.JBObject;
import com.javabaas.exception.JBException;

/**
 * Created by xueshukai on 15/12/18 下午2:54.
 */
public interface SaveCallback {
    void done(JBObject object);
    void error(JBException e);
}
