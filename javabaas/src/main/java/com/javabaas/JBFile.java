package com.javabaas;


import java.io.File;

import com.javabaas.callback.FileUploadCallback;
import com.javabaas.callback.ResponseListener;
import com.javabaas.exception.JBException;

/**
 * Created by xueshukai on 15/10/9 下午5:31.
 */
public class JBFile extends JBObject {
    private String className = "_File";

    private String path;
    private byte[] data;
    private File file;
    private String name;

    public JBFile() {
    }

    public JBFile(File file) {
        this.file = file;
    }

    public JBFile(byte[] bytes) {
        this.data = bytes;
    }

    public static JBFile createWithoutData(String id){
        JBFile jbObject = new JBFile();
        jbObject.setId(id);
        return jbObject;
    }

    public File getFile(){
        return file;
    }

    @Override
    public String getClassName() {
        return className;
    }

    public void saveInBackground(FileUploadCallback callback) {
        getUploadToken(new ResponseListener<CustomResponse>() {
            @Override
            public void onResponse(CustomResponse entity) {
                if (uploader != null)
                    uploader.upload(entity ,JBFile.this,callback);
            }

            @Override
            public void onError(JBException e) {
                callback.error(e);
            }
        });
    }

    private void getUploadToken(ResponseListener<CustomResponse> listener) {
        JBCloud.getObjectManager(null).customJsonRequest(JBCloud.applicationContext, false , listener, "/api/file/getToken?fileName="+filename+"&platform=" + platform, IObjectManager.Method.GET, null);
    }

    private static IUploader uploader = null;
    private static String platform , filename;
    public static void setUploader(IUploader uploader){
        JBFile.uploader = uploader;
    }
    static {
        if (uploader == null){
            uploader = new QiNiuUploader();
            platform = "qiniu";
            filename = "android_file";
        }
    }
    public static void setPlatform(String platform){
        JBFile.platform = platform;
    }

    public static void setFilename(String filename){
        JBFile.filename = filename;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public static JBFile withFile(File file) {
        return new JBFile(file);
    }

    public static JBFile withByte(byte[] bytes){
        return new JBFile(bytes);
    }

    public String getUrl() {
        return (String) get("url");
    }

    public String mimeType() {
        return null;
    }
}