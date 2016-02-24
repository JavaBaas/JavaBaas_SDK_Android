package com.javabaas.callback;

import com.javabaas.exception.JBException;

/**
 * Created by xueshukai on 16/2/24 上午11:07.
 */
public interface GetInstallationIdCallback {
    void done(String id);
    void error(JBException e);
}
