package com.javabaas.callback;

import com.javabaas.JBFile;
import com.javabaas.exception.JBException;

/**
 * Created by xueshukai on 16/1/4 下午12:50.
 */
public interface FileUploadCallback {
    void done(JBFile jbFile);
    void error(JBException e);
    void onProgress(double percent);
}
