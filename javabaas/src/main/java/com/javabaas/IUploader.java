package com.javabaas;

import com.javabaas.callback.FileUploadCallback;

/**
 * Created by xueshukai on 16/2/1 下午3:16.
 */
public interface IUploader {
    void upload(CustomResponse entity , JBFile jbFile , FileUploadCallback callback);
}
