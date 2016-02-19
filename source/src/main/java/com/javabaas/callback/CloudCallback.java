package com.javabaas.callback;

import com.javabaas.ResponseEntity;
import com.javabaas.exception.JBException;

/**
 * Created by xueshukai on 15/12/30 上午11:07.
 */
public interface CloudCallback {
    void done(ResponseEntity responseEntity);
    void error(JBException e , ResponseEntity responseEntity);
}
