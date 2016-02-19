package com.javabaas;

import com.alibaba.fastjson.JSON;
import com.javabaas.exception.JBException;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;

import org.json.JSONException;
import org.json.JSONObject;

import com.javabaas.callback.FileUploadCallback;
import com.javabaas.util.Utils;

/**
 * Created by xueshukai on 16/2/1 下午3:17.
 */
public class QiNiuUploader implements IUploader {
    @Override
    public void upload(CustomResponse entity, JBFile jbFile, FileUploadCallback callback) {
        UploadManager uploadManager = new UploadManager();
        String name = "", token = "";
        try {
            JSONObject jsonObject = new JSONObject(entity.getData());
            JSONObject data = jsonObject.getJSONObject("data");
            name = data.getString("name");
            token = data.getString("token");
        } catch (JSONException e) {
            if (callback != null){
                JBException jbException = new JBException(JBException.PARSE_ERROR , e.getMessage());
                callback.error(jbException);
            }
            return;
        }
        if (Utils.isBlankString(token) || Utils.isBlankString(name))
            return;

        UpCompletionHandler completionHandler = new UpCompletionHandler() {
            @Override
            public void complete(String name, ResponseInfo responseInfo, JSONObject jsonObject) {
                //如果请求成功
                if (responseInfo.isOK()) {
                    try {
                        JSONObject data = jsonObject.getJSONObject("data");
                        JSONObject file = data.getJSONObject("file");
                        if (file != null)
                            jbFile.putAll(JSON.parseObject(file.toString() , JBObject.class));
                        if (callback != null)
                            callback.done(jbFile);
                    } catch (JSONException e) {
                        if (callback != null){
                            JBException jbException = new JBException(JBException.PARSE_ERROR , e.getMessage());
                            callback.error(jbException);
                        }
                    }

                } else {
                    if (callback != null){
                        JBException jbException = new JBException(JBException.UPLOAD_ERROR, "上传失败 code:" + responseInfo.statusCode);
                        callback.error(jbException);
                    }
                }
            }
        };
        UpProgressHandler upProgressHandler = new UpProgressHandler(){

            @Override
            public void progress(String key, double percent) {
                if (callback != null)
                    callback.onProgress(percent);
            }
        };
        if (jbFile.getData() != null)
            uploadManager.put(jbFile.getData(), name, token, completionHandler, new UploadOptions(null, null, false, upProgressHandler,
                    null));
        else if (jbFile.getFile() != null){
            uploadManager.put(jbFile.getFile(), name, token, completionHandler, new UploadOptions(null, null, false, upProgressHandler,
                    null));
        }
    }
}
