package com.javabaas.callback;


import com.javabaas.exception.JBException;

/**
 * Created by xueshukai on 15/10/9 下午5:46.
 */
public interface FileSaveCallback {

    /**
     * 上传成功后会回调的方法
     * @param  url 上传文件在七牛服务器上存储的url
     */
    void done(String url);

    void error(JBException e);
}
