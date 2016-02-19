package com.javabaas.callback;

import com.javabaas.JBUser;
import com.javabaas.exception.JBException;

/**
 * Created by xueshukai on 16/1/27 上午11:30.
 */
public interface LoginCallback {
    void done(JBUser jbUser);
    void error(JBException e);
}
