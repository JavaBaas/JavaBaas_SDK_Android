package com.javabaas;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.alibaba.fastjson.JSONObject;
import com.javabaas.callback.ResponseListener;
import com.javabaas.exception.JBException;
import com.javabaas.util.Utils;
import com.orhanobut.logger.Logger;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Map;

/**
 * Created by xueshukai on 15/12/18 下午3:05.
 */
public class ObjectManagerImp extends IObjectManager {

    static OkHttpClient client = new OkHttpClient();

    public ObjectManagerImp(Context context, String tableName) {
        super(context, tableName);
    }

    @Override
    public void customJsonRequest(Context context , boolean isSync, final ResponseListener<CustomResponse> listener, String url, int method, String requestBody) {
        if (Utils.isBlankString(host)){
            throw new RuntimeException("请在JBCloud.init()配置host url");
        }
        Logger.d(url);
        if (url.startsWith("/")) {
            url = host + url;
        }else{
            url = host + "/" + url;
        }

        Request.Builder builder = new Request.Builder();
        builder.url(url);

        Map<String, String> requestHeader = createRequestHeader();
        for (String s : requestHeader.keySet()) {
            builder.header(s , requestHeader.get(s));
        }
        switch (method){
            case Method.POST:
                builder.post(RequestBody.create(MediaType.parse("application/json;charset=UTF-8") , requestBody));
                break;
            case Method.PUT:
                builder.put(RequestBody.create(MediaType.parse("application/json;charset=UTF-8") , requestBody));
                break;
            case Method.DELETE:
                builder.delete();
                break;
        }
        Call call = client.newCall(builder.build());

        if (isSync){
            try {
                Response response = call.execute();
                String data = response.body().string();
                CustomResponse customResponse = new CustomResponse(data);
                customResponse.setStatusCode(response.code());
                if (response.code() == 200) {//成功
                    //回调成功
                    if (listener != null)
                        listener.onResponse(customResponse);
                } else {//失败
                    JSONObject jsonObject = JSONObject.parseObject(data);
                    JBException jbException;
                    if (jsonObject != null) {
                        jbException = new JBException(jsonObject.getString("message"));
                        jbException.responseErrorMsg = jsonObject.getString("message");
                        jbException.responseErrorCode = jsonObject.getInteger("code");
                    }else {
                        jbException = new JBException();
                    }
                    jbException.errorCode = JBException.SERVER_ERROR;
                    //回调失败
                    if (listener != null)
                        listener.onError(jbException);
                }
            } catch (IOException e) {
                JBException jbException = new JBException(e.getMessage());
                jbException.errorCode = JBException.NETWORK_ERROR;
                //回调网络错误
                if (listener != null)
                    listener.onError(jbException);
            }
        }else {
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    JBException jbException = new JBException(e.getMessage());
                    jbException.errorCode = JBException.NETWORK_ERROR;
                    //回调网络错误
                    Message msg = handler.obtainMessage(1);
                    msg.obj = new Object[]{listener , jbException};
                    msg.sendToTarget();
                }

                @Override
                public void onResponse(com.squareup.okhttp.Response response) throws IOException {
                    String data = response.body().string();
                    CustomResponse customResponse = new CustomResponse(data);
                    customResponse.setStatusCode(response.code());
                    if (response.code() == 200) {//成功
                        //回调成功
                        Message msg = handler.obtainMessage(0);
                        msg.obj = new Object[]{listener , customResponse};
                        msg.sendToTarget();
                    } else {//失败
                        JBException jbException = new JBException();
                        JSONObject jsonObject = JSONObject.parseObject(data);
                        jbException.errorCode = JBException.SERVER_ERROR;
                        if (jsonObject != null) {
                            try {
                                jbException.responseErrorMsg = jsonObject.getString("message");
                                jbException.responseErrorCode = jsonObject.getInteger("code");
                            }catch (Exception e){
                            }
                        }
                        //回调失败
                        Message msg = handler.obtainMessage(1);
                        msg.obj = new Object[]{listener , jbException};
                        msg.sendToTarget();
                    }
                }
            });
        }

    }

    static Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Object[] obj = (Object[]) msg.obj;
            ResponseListener<CustomResponse> listener = (ResponseListener<CustomResponse>) obj[0];
            if (msg.what == 1){
                if (listener != null)
                    listener.onError((JBException) obj[1]);
            }else {
                if (listener != null)
                    listener.onResponse((CustomResponse) obj[1]);
            }
            return false;
        }
    });

}
