package com.javabaas.callback;

import com.javabaas.JBUser;
import com.javabaas.exception.JBException;

/**
 * Created by xueshukai on 16/1/26 下午5:39.
 */
public interface SignUpCallback {
    void done(JBUser jbUser);
    void error(JBException e);
}
