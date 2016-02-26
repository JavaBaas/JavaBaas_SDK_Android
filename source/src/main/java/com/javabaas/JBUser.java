package com.javabaas;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.javabaas.cache.JBPersistenceUtils;
import com.javabaas.callback.LoginCallback;
import com.javabaas.callback.RequestCallback;
import com.javabaas.callback.ResponseListener;
import com.javabaas.callback.SaveCallback;
import com.javabaas.callback.SignUpCallback;
import com.javabaas.exception.JBException;
import com.javabaas.util.Utils;

/**
 * Created by xueshukai on 15/9/28 上午11:21.
 */
public class JBUser extends JBObject {

    private static JBUser currentUser;

    public JBUser(String id, String sessionToken) {
        className = "_User";
        put("sessionToken", sessionToken);
        setId(id);
    }

    public JBUser() {
        className = "_User";
    }

    private static File currentUserArchivePath() {
        return new File(JBPersistenceUtils.getPaasDocumentDir() + "/currentUser");
    }

    public static JBUser getCurrentUser() {
        if (currentUser == null) {
            String user = JBPersistenceUtils.readContentFromFile(currentUserArchivePath());
            if (!Utils.isBlankString(user)) {
                JSONObject jsonObject = JSONObject.parseObject(user);
                currentUser = new JBUser();
                currentUser.putAll(jsonObject);
                IObjectManager.parseJBObject(currentUser);
            }
        }
        return currentUser;
    }

    public String getSessionToken() {
        return (String) get("sessionToken");
    }

    public void setSessionToken(String sessionToken) {
        put("sessionToken", sessionToken);
    }

    public String getUsername() {
        return (String) get("username");
    }

    public void setUsername(String username) {
        put("username", username);
    }

    public String getPassword() {
        return (String) get("password");
    }

    public void setPassword(String password) {
        put("password", password);
    }

    public String getEmail() {
        return (String) get("email");
    }

    public void setEmail(String email) {
        put("email", email);
    }

    public String getPhone() {
        return (String) get("phone");
    }

    public void setPhone(String phone) {
        put("phone", phone);
    }

    @Override
    public String getClassName() {
        return className;
    }

    public static JBUser createWithoutData(String id) {
        JBUser jbObject = new JBUser();
        jbObject.setId(id);
        return jbObject;
    }

    public static class JBThirdPartyUserAuth implements Serializable {
        String accessToken;
        String snsType;
        String userId;
        public static final String SNS_TENCENT_WEIBO = "qq";
        public static final String SNS_SINA_WEIBO = "weibo";
        public static final String SNS_TENCENT_WEIXIN = "weixin";

        public JBThirdPartyUserAuth(String accessToken, String snstype, String userId) {
            this.accessToken = accessToken;
            this.snsType = snstype;
            this.userId = userId;
        }

        protected static String platformUserIdTag(String type) {
            return !"qq".equalsIgnoreCase(type) && !"weixin".equalsIgnoreCase(type) ? "uid" : "openid";
        }

        public String getAccessToken() {
            return this.accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getUserId() {
            return this.userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getSnsType() {
            return this.snsType;
        }

        public void setSnsType(String snsType) {
            this.snsType = snsType;
        }

    }

    public void signUpInBackground(final SignUpCallback callback) {
        saveInBackground(new SaveCallback() {
            @Override
            public void done(JBObject object) {
                if (callback != null) {
                    callback.done((JBUser) object);
                }
            }

            @Override
            public void error(JBException e) {
                if (callback != null)
                    callback.error(e);
            }
        });
    }

    public JBUser signUp() throws JBException {
        JBUser user = (JBUser) save();
        changeCurrentUser(user, true);
        return user;
    }

    public static void loginWithUsernameInBackground(String username, String password, LoginCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        JBCloud.getObjectManager(null).userLogin(params, IObjectManager.LOGIN_WITH_USERNAME_TYPE,null ,false, callback);
    }

    public static JBUser loginWithUsername(String username, String password) throws JBException {
        HashMap<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        final Object[] objects = new Object[2];
        JBCloud.getObjectManager(null).userLogin(params, IObjectManager.LOGIN_WITH_USERNAME_TYPE, null , true, new LoginCallback() {
            @Override
            public void done(JBUser jbUser) {
                objects[0] = jbUser;
                changeCurrentUser(jbUser, true);
            }

            @Override
            public void error(JBException e) {
                objects[1] = e;
            }
        });
        if (objects[1] != null)
            throw ((JBException) objects[1]);
        return (JBUser) objects[0];
    }

    public static void loginWithSnsInBackground(JBThirdPartyUserAuth auth, LoginCallback callback){
        HashMap<String, String> params = new HashMap<>();
        params.put("accessToken", auth.accessToken);
        params.put("uid", auth.getUserId());
        JBCloud.getObjectManager(null).userLogin(params, IObjectManager.LOGIN_WITH_SNS_TYPE,auth.getSnsType() ,false, callback);
    }

    public static JBUser loginWithSns(JBThirdPartyUserAuth auth) throws JBException {
        HashMap<String, String> params = new HashMap<>();
        params.put("accessToken", auth.accessToken);
        params.put("uid", auth.getUserId());
        final Object[] objects = new Object[2];
        JBCloud.getObjectManager(null).userLogin(params, IObjectManager.LOGIN_WITH_SNS_TYPE, auth.getSnsType(),true, new LoginCallback() {
            @Override
            public void done(JBUser jbUser) {
                objects[0] = jbUser;
                changeCurrentUser(jbUser, true);
            }

            @Override
            public void error(JBException e) {
                objects[1] = e;
            }
        });
        if (objects[1] != null)
            throw ((JBException) objects[1]);
        return (JBUser) objects[0];
    }

    public static void bindWithSnsInBackground(JBThirdPartyUserAuth auth , String userId, RequestCallback callback) {
        JBCloud.getObjectManager(null).bindWithSns(auth , userId ,false , callback);
    }

    public static void bindWithSns(JBThirdPartyUserAuth auth , String userId) throws JBException {
        final Object[] objects = new Object[1];
        JBCloud.getObjectManager(null).bindWithSns(auth, userId ,true, new RequestCallback() {
            @Override
            public void done() {
            }

            @Override
            public void error(JBException e) {
                objects[0] = e;
            }
        });
        if (objects[0] != null)
            throw (JBException) objects[0];
    }

    public void saveInBackground(final SaveCallback callback) {
        JBCloud.getObjectManager(className).saveObject(this, false, getId(), new SaveCallback() {
            @Override
            public void done(JBObject object) {
                changeCurrentUser((JBUser) object, true);
                if (callback != null)
                    callback.done(object);
            }

            @Override
            public void error(JBException e) {
                if (callback != null)
                    callback.error(e);
            }
        });
    }

    public static void logout() {
        changeCurrentUser(null , true);
    }

    public static void updatePassword(String oldPassword , String newPassword , final RequestCallback callback){
        if (JBUser.getCurrentUser() == null) {
            if (callback != null){
                JBException exception = new JBException("用户未登录");
                callback.error(exception);
            }
            return;
        }
        HashMap<String, String> params = new HashMap<>();
        params.put("oldPassword" , oldPassword);
        params.put("newPassword" , newPassword);
        JBCloud.getObjectManager(null).updatePassword(params , new ResponseListener<CustomResponse>() {
            @Override
            public void onResponse(CustomResponse entity) {
                JSONObject jsonObject = JSON.parseObject(entity.getData());
                JSONObject data = jsonObject.getJSONObject("data");
                String sessionToken = data.getString("sessionToken");
                JBUser.getCurrentUser().setSessionToken(sessionToken);
                if (callback != null)
                    callback.done();
            }

            @Override
            public void onError(JBException e) {
                if (callback != null)
                    callback.error(e);
            }
        });
    }

    public static synchronized void changeCurrentUser(JBUser newUser, boolean save) {
        if (newUser != null) {
            newUser.setPassword(null);
        }

        File currentUserArchivePath = currentUserArchivePath();
        if (newUser != null && save) {
            try {
                String e = JSON.toJSONString(newUser);
                JBPersistenceUtils.saveContentToFile(e, currentUserArchivePath);
            } catch (Exception e) {
            }
        } else if (save) {
            JBPersistenceUtils.removeLock(currentUserArchivePath.getAbsolutePath());
            currentUserArchivePath.delete();
        }
        currentUser = newUser;
    }

}
