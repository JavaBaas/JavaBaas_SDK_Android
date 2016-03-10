package com.javabaas.exception;

/**
 * Created by xueshukai on 15/9/29 下午2:55.
 */
public class JBException extends Exception {
    /**
     * 认证失败
     */
    public static int AUTH_FAILURE_ERROR = 1;

    /**
     * 网络错误
     */
    public static int NETWORK_ERROR = 2;

    /**
     * 解析错误
     */
    public static int PARSE_ERROR = 3;

    /**
     * 服务器错误
     */
    public static int SERVER_ERROR = 4;

    /**
     * 云方法错误
     */
    public static int CLOUD_ERROR = 5;

    /**
     * 上传错误
     */
    public static int UPLOAD_ERROR = 6;

    //responseErrorCode
    public static int SESSION_TOKEN_ERROR_CODE = 1310;

    public int errorCode;
    public int responseErrorCode;
    public String responseErrorMsg;

    public JBException() {
    }

    public JBException(String detailMessage) {
        super(detailMessage);
    }

    public JBException(int errorCode , String detailMessage) {
        super(detailMessage);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public int getResponseErrorCode() {
        return responseErrorCode;
    }

    public String getResponseErrorMsg() {
        return responseErrorMsg;
    }
}
