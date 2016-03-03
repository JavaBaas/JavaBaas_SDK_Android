package com.javabaas;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.javabaas.callback.GetInstallationIdCallback;
import com.javabaas.util.Utils;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.javabaas.callback.CloudCallback;
import com.javabaas.callback.ResponseListener;
import com.javabaas.exception.JBException;
import com.javabaas.util.SharedPreferencesUtils;

/**
 * Created by xueshukai on 15/10/10 下午2:16.
 */
public class JBCloud {
    public static Context applicationContext;

    static Class<? extends IObjectManager> objectManagerClass = null;

    public static final int CLOUD_FUNCTION_SUCCESS = 0;
    public static final int CLOUD_FUNCTION_ERROR = 1;

    /**
     *
     * @param context
     * @param appKey
     * @param appId
     * @param host base url
     */
    public static void init(Context context , String appKey , String appId , String host , GetInstallationIdCallback callback) {
        applicationContext = context;
        IObjectManager.appKey = appKey;
        IObjectManager.appId = appId;
        IObjectManager.host = host;
        syncTimestamp();
        getInstallationId(callback);
        setObjectManager(ObjectManagerImp.class);
    }

    public static void showLog(){
        Utils.showLog();
    }

    public static void setObjectManager(Class<? extends IObjectManager> objectManager){
        JBCloud.objectManagerClass = objectManager;
    }

    static IObjectManager getObjectManager(String tableName){
        if (objectManagerClass == null)
            setObjectManager(ObjectManagerImp.class);
        Class[] paramTypes = { Context.class, String.class};
        try {
            Constructor con = objectManagerClass.getConstructor(paramTypes);
            return ((IObjectManager) con.newInstance(applicationContext, tableName));
        } catch (Exception e) {
            throw new RuntimeException("IObjectManager illegal " , e);
        }
    }

    public static void callFunctionInBackground(String name, Map<String, Object> params, final CloudCallback listener) {
        String url = "/api/cloud/" + name;
        StringBuilder stringBuffer = new StringBuilder(url);
        stringBuffer.append("?");
        for (String s : params.keySet()) {
            Object o = params.get(s);
            stringBuffer.append(s).append("=").append(String.valueOf(o)).append("&");
        }
        JBCloud.getObjectManager(null).customJsonRequest(applicationContext, false ,new ResponseListener<CustomResponse>() {
            @Override
            public void onResponse(CustomResponse entity) {
                JSONObject response = JSON.parseObject(entity.getData());
                int code = response.getIntValue("code");
                ResponseEntity responseEntity = new ResponseEntity();
                responseEntity.setCode(code);
                responseEntity.setData(response.getJSONObject("data"));
                responseEntity.setMessage(response.getString("message"));
                if (listener == null)
                    return;
                if (code == CLOUD_FUNCTION_SUCCESS){
                    listener.done(responseEntity);
                }else if (code == CLOUD_FUNCTION_ERROR){
                    JBException jbException = new JBException(response.getString("message"));
                    jbException.errorCode = JBException.CLOUD_ERROR;
                    listener.error(jbException , responseEntity);
                }else {
                    listener.done(responseEntity);
                }
            }

            @Override
            public void onError(JBException e) {
                if (listener != null)
                    listener.error(e , null);
            }
        }, stringBuffer.toString(), IObjectManager.Method.GET, null);
    }

    //获取InstallationID
    public static void getInstallationId(final GetInstallationIdCallback callback) {
        String installationId = (String) SharedPreferencesUtils.get(applicationContext, "installationId" , "");
        if (!TextUtils.isEmpty(installationId)){
            if (callback != null)
                callback.done(installationId);
            return;
        }
        String url = "/api/installation";
        HashMap<String, Object> params = new HashMap<>();
        params.put("deviceToken" , getDeviceId());
        params.put("deviceType" , "android");
        JBCloud.getObjectManager(null).customJsonRequest(applicationContext, false, new ResponseListener<CustomResponse>() {
            @Override
            public void onResponse(CustomResponse entity) {
                JSONObject response = JSON.parseObject(entity.getData());
                JSONObject data = response.getJSONObject("data");
                String id = data.getString("id");
                Utils.printLog("InstallationId 获取成功 "+id );
                if (callback != null)
                    callback.done(id);
                if (id != null)
                    SharedPreferencesUtils.put(applicationContext, "installationId", id);
            }

            @Override
            public void onError(JBException e) {
                Utils.printLog("InstallationId 获取失败 "+ e.getMessage());
                if (callback != null)
                    callback.error(e);
            }
        },url, IObjectManager.Method.POST, JSON.toJSONString(params));
    }

    //同步时间戳
    public static void syncTimestamp() {
        String url = "/time";
        Request.Builder builder = new Request.Builder();
        builder.get().url(IObjectManager.host + url);
        Call call = ObjectManagerImp.client.newCall(builder.build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Utils.printLog("时间戳获取失败 " + e.getMessage());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Utils.printLog("时间戳获取成功 " );
                SharedPreferencesUtils.put(applicationContext, "timeDiff", Long.valueOf(response.body().string()) - System.currentTimeMillis());
            }
        });
    }

    //获取设备唯一ID
    public static String getDeviceId(){
        final TelephonyManager tm = (TelephonyManager) applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(applicationContext.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        return deviceUuid.toString();
    }

}
