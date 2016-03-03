package com.javabaas;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.javabaas.cache.JBCacheManager;
import com.javabaas.callback.CountCallback;
import com.javabaas.callback.DeleteCallback;
import com.javabaas.callback.FindCallback;
import com.javabaas.callback.LoginCallback;
import com.javabaas.callback.RequestCallback;
import com.javabaas.callback.ResponseListener;
import com.javabaas.callback.SaveCallback;
import com.javabaas.exception.JBException;
import com.javabaas.util.SharedPreferencesUtils;
import com.javabaas.util.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xueshukai on 15/12/18 上午11:49.
 */
public abstract class IObjectManager {

    static String appKey = "";
    static String appId = "";
    static String host = "";
    static final int LOGIN_WITH_USERNAME_TYPE = 0;
    static final int LOGIN_WITH_SNS_TYPE = 1;
    static final int LOGIN_WITH_PHONE_TYPE = 2;

    private String tableName;
    private Context context;
    private static long timeDiff;

    IObjectManager(Context context, String tableName) {
        this.tableName = tableName;
        this.context = context;
    }


    /**
     * 保存或更新对象
     *
     * @param object   对象
     * @param id       对象id （更新对象时使用）
     * @param listener
     */
    void saveObject(final JBObject object, boolean isSync, String id, final SaveCallback listener) {
        String url;
        if (tableName.equals("_User")) {
            url = "/api/user";
        } else
            url = "/api/object/" + tableName;
        int method = Method.POST;
        if (!Utils.isBlankString(id)) {
            url = url + "/" + id;
            method = Method.PUT;
        }
        HashMap clone = (HashMap) object.clone();
        for (Map.Entry<String, Object> stringObjectEntry : object.entrySet()) {
            Object value = stringObjectEntry.getValue();
            if (value instanceof JBFile || (value instanceof Map && ((Map) value).get("__type") != null && ((Map) value).get("__type").equals("File"))) {
                JBFile file = new JBFile();
                file.putAll((Map) value);
                clone.put(stringObjectEntry.getKey(), Utils.mapFromFileObject(file));
            } else if (value instanceof JBObject) {
                clone.put(stringObjectEntry.getKey(), Utils.mapFromPointerObject((JBObject) value));
            } else if (value instanceof JBAcl) {
                clone.putAll(((JBAcl) value).getACLMap());
            }
        }
        JSONObject jsonObject = new JSONObject(true);
        jsonObject.putAll(clone);
        String requestBody = jsonObject.toString();

        customJsonRequest(context, isSync, new ResponseListener<CustomResponse>() {
            @Override
            public void onResponse(CustomResponse entity) {
                JSONObject response = JSON.parseObject(entity.getData());
                JSONObject data = response.getJSONObject("data");
                object.putAll(data);
                if (listener != null) {
                    listener.done(object);
                }
            }

            @Override
            public void onError(JBException e) {
                if (listener != null)
                    listener.error(e);
            }
        }, url, method, requestBody);
    }


    /**
     * 删除对象
     *
     * @param id
     * @param deleteCallback
     */
    void deleteObject(String id, boolean isSync, final DeleteCallback deleteCallback) {
        String url = "/api/object/" + tableName + "/" + id;
        customJsonRequest(context, isSync, new ResponseListener<CustomResponse>() {
            @Override
            public void onResponse(CustomResponse entity) {
                if (deleteCallback != null) {
                    deleteCallback.done();
                }
            }

            @Override
            public void onError(JBException e) {
                if (deleteCallback != null)
                    deleteCallback.error(e);
            }
        }, url, Method.DELETE, null);
    }

    /**
     * 通过query删除
     *
     * @param parameters
     * @param isSync
     * @param deleteCallback
     */
    void deleteByQuery(final Map<String, String> parameters, boolean isSync, final DeleteCallback deleteCallback) {

        customJsonRequest(context, isSync, new ResponseListener<CustomResponse>() {
            @Override
            public void onResponse(CustomResponse entity) {
                if (deleteCallback != null) {
                    deleteCallback.done();
                }
            }

            @Override
            public void onError(JBException e) {
                if (deleteCallback != null)
                    deleteCallback.error(e);
            }
        }, "/api/object/" + tableName + "/deleteByQuery" + parseParameter(parameters), Method.DELETE, null);
    }

    /**
     * 对象查询
     *
     * @param parameters
     * @param findCallback
     * @param cacheType
     */
    void objectQuery(final Map<String, String> parameters, boolean isSync, final FindCallback<JBObject> findCallback, JBQuery.CachePolicy cacheType) {
        final StringBuilder url = new StringBuilder("/api/object/" + tableName);

        url.append(parseParameter(parameters));

        if (cacheType == JBQuery.CachePolicy.CACHE_THEN_NETWORK || cacheType == JBQuery.CachePolicy.CACHE_ONLY) {
            JBCacheManager.sharedInstance().get(url.toString(), -1, tableName, JBObject.class, findCallback);
            if (cacheType == JBQuery.CachePolicy.CACHE_ONLY)
                return;
        }

        customJsonRequest(context, isSync, new ResponseListener<CustomResponse>() {
            @Override
            public void onResponse(CustomResponse entity) {
                List<JBObject> jbObjects = JSON.parseArray(entity.getData(), JBObject.class);
                for (JBObject jbObject : jbObjects) {
                    parseJBObject(jbObject);
                    jbObject.setClassName(tableName);
                }
                JBCacheManager.sharedInstance().save(url.toString(), entity.getData(), tableName);
                if (findCallback != null)
                    findCallback.done(jbObjects);
            }

            @Override
            public void onError(JBException e) {
                if (findCallback != null)
                    findCallback.error(e);
            }
        }, url.toString(), Method.GET, null);
    }


    /**
     * count查询
     *
     * @param parameters
     * @param callback
     */
    void countQuery(final Map<String, String> parameters, boolean isSync, final CountCallback callback) {
        customJsonRequest(context, isSync, new ResponseListener<CustomResponse>() {
            @Override
            public void onResponse(CustomResponse entity) {
                JSONObject data = JSON.parseObject(entity.getData());
                JSONObject dataJSONObject = data.getJSONObject("data");
                int count = dataJSONObject.getIntValue("count");
                if (callback != null)
                    callback.done(count);
            }

            @Override
            public void onError(JBException e) {
                if (callback != null)
                    callback.error(e);
            }
        }, "/api/object/" + tableName + "/count" + parseParameter(parameters), Method.GET, null);
    }

    /**
     * 自增
     *
     * @param incrementKeyAmount
     * @param objectId
     * @param isSync
     * @param callback
     */
    void incrementObjectKey(Map<String, Integer> incrementKeyAmount, String objectId, boolean isSync, final RequestCallback callback) {
        JSONObject jsonObject = new JSONObject(true);
        jsonObject.putAll(incrementKeyAmount);
        customJsonRequest(context, isSync, new ResponseListener<CustomResponse>() {
            @Override
            public void onResponse(CustomResponse entity) {
                if (callback != null)
                    callback.done();
            }

            @Override
            public void onError(JBException e) {
                if (callback != null)
                    callback.error(e);
            }
        }, "/api/object/" + tableName + "/" + objectId + "/inc", Method.PUT, jsonObject.toJSONString());
    }

    /**
     * 用户登录
     *
     * @param params
     * @param type
     * @param value
     * @param isSync
     * @param callback
     */
    void userLogin(Map<String, String> params, int type, String value, boolean isSync, final LoginCallback callback) {
        String url = "/api/user/";
        int method = Method.GET;
        String requestBody = null;
        switch (type) {
            case LOGIN_WITH_USERNAME_TYPE:
                url += "login";
                break;
            case LOGIN_WITH_SNS_TYPE:
                url = url + "loginWithSns/" + value;
                method = Method.POST;
                JSONObject jsonObject = new JSONObject();
                jsonObject.putAll(params);
                requestBody = jsonObject.toString();
                break;
        }

        customJsonRequest(context, isSync, new ResponseListener<CustomResponse>() {
            @Override
            public void onResponse(CustomResponse entity) {
                JSONObject data = JSON.parseObject(entity.getData());
                if (callback != null) {
                    JBUser jbUser = new JBUser();
                    jbUser.putAll(data);
                    JBUser.changeCurrentUser(jbUser, true);
                    callback.done(jbUser);
                }
            }

            @Override
            public void onError(JBException e) {
                if (callback != null)
                    callback.error(e);
            }
        }, method == Method.GET ? url + parseParameter(params) : url, method, requestBody);
    }

    /**
     * 绑定第三方平台
     *
     * @param auth
     * @param userId
     * @param isSync
     * @param callback
     */
    void bindWithSns(JBUser.JBThirdPartyUserAuth auth, String userId, boolean isSync, final RequestCallback callback) {
        String url = "/api/user/" + userId + "/binding/" + auth.getSnsType();
        HashMap<String, String> params = new HashMap<>();
        params.put("accessToken", auth.accessToken);
        params.put("uid", auth.getUserId());
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(params);
        customJsonRequest(context, isSync, new ResponseListener<CustomResponse>() {
            @Override
            public void onResponse(CustomResponse entity) {
                if (callback != null)
                    callback.done();
            }

            @Override
            public void onError(JBException e) {
                if (callback != null)
                    callback.error(e);
            }
        }, url, Method.POST, jsonObject.toString());
    }

    /**
     * 更新密码
     *
     * @param params
     * @param callback
     */
    void updatePassword(Map<String, String> params, ResponseListener<CustomResponse> callback) {

        String url = "/api/user/" + JBUser.getCurrentUser().getId() + "/updatePassword";
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(params);
        customJsonRequest(context, false, callback, url, Method.PUT, jsonObject.toString());
    }

    public abstract void customJsonRequest(final Context context, boolean isSync, final ResponseListener<CustomResponse> listener, final String url, final int method, final String requestBody);

    protected static Map<String, String> createRequestHeader() {
        if (timeDiff == 0L)
            timeDiff = (long) SharedPreferencesUtils.get(JBCloud.applicationContext, "timeDiff", 0L);
        String timestamp = String.valueOf(System.currentTimeMillis() + timeDiff);
        Map<String, String> headers = new HashMap<>();
        headers.put("JB-AppId", appId);
        headers.put("JB-Timestamp", timestamp);
        headers.put("JB-Plat", "android");
        headers.put("JB-Sign", Utils.MD5(appKey + ":" + timestamp));
        headers.put("Content-Type", "application/json;charset=UTF-8");
        if (JBUser.getCurrentUser() != null)
            headers.put("JB-SessionToken", JBUser.getCurrentUser().getSessionToken());
        return headers;
    }

    public static void parseJBObject(JBObject jbObject) {
        for (String s : jbObject.keySet()) {
            Object o = jbObject.get(s);
            if (s.equals("acl")) {
                JBAcl acl = new JBAcl(((Map) o));
                jbObject.put(s, acl);
                continue;
            }
            if (o instanceof Map) {
                Map map = (Map) o;
                JBObject object = new JBObject((String) map.get("className"));
                object.putAll(map);
                parseJBObject(object);
                jbObject.put(s, object);
            }
        }
    }

    private String parseParameter(Map<String, String> parameters) {
        StringBuilder url = new StringBuilder();
        if (parameters != null) {
            url.append("?");
            for (Map.Entry<String, String> stringStringEntry : parameters.entrySet()) {
                url.append(stringStringEntry.getKey()).append("=").append(stringStringEntry.getValue()).append("&");
            }
        }
        return url.toString();
    }

    public interface Method {
        int GET = 0;
        int POST = 1;
        int PUT = 2;
        int DELETE = 3;
        int HEAD = 4;
        int OPTIONS = 5;
        int TRACE = 6;
        int PATCH = 7;
    }
}
