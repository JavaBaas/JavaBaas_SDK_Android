package com.javabaas;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.javabaas.callback.DeleteCallback;
import com.javabaas.callback.RequestCallback;
import com.javabaas.callback.SaveCallback;
import com.javabaas.exception.JBException;
import com.javabaas.util.Utils;

import org.json.JSONArray;

/**
 * Created by xueshukai on 15/10/8 上午10:36.
 */
public class JBObject extends LinkedHashMap<String, Object> {
    protected String className;

    public JBObject() {
    }

    public JBObject(String className) {
        this.className = className;
    }

    public void setId(String id) {
        this.put("_id", id);
    }

    public String getId() {
        return (String) get("_id");
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public JBObject getJBObject(String key) {
        return (JBObject) get(key);
    }

    public Date getCreatedAt() {
        return new Date((long) get("createdAt"));
    }

    public JBFile getJBFile(String key){
        Object o = get(key);
        if (o instanceof JSONObject){
            JBFile jbFile = new JBFile();
            jbFile.putAll(((JSONObject) o));
            return jbFile;
        }else if (o instanceof JBObject){
            JBFile jbFile = new JBFile();
            jbFile.putAll(((JBObject) o));
            return jbFile;
        }
        return null;
    }

    public int getInt(String key){
        Number v = (Number)this.get(key);
        return v != null?v.intValue():0;
    }

    public double getDouble(String key) {
        Number number = (Number)this.get(key);
        return number != null?number.doubleValue():0.0D;
    }

    public JSONArray getJSONArray(String key) {
        Object list = this.get(key);
        if(list == null) {
            return null;
        } else if(list instanceof JSONArray) {
            return (JSONArray)list;
        } else {
            JSONArray array;
            if(list instanceof Collection) {
                array = new JSONArray((Collection)list);
                return array;
            } else if(!(list instanceof Object[])) {
                return null;
            } else {
                array = new JSONArray();
                Object[] arr$ = (Object[])list;
                int len$ = arr$.length;

                for(int i$ = 0; i$ < len$; ++i$) {
                    Object obj = arr$[i$];
                    array.put(obj);
                }

                return array;
            }
        }
    }

    public org.json.JSONObject getJSONObject(String key) {
        Object object = this.get(key);
        if(object instanceof org.json.JSONObject) {
            return (org.json.JSONObject)object;
        } else {
            String jsonString = JSON.toJSONString(object);
            org.json.JSONObject jsonObject = null;

            try {
                jsonObject = new org.json.JSONObject(jsonString);
                return jsonObject;
            } catch (Exception var6) {
                throw new IllegalStateException("Invalid json string", var6);
            }
        }
    }

    public List getList(String key) {
        return (List)this.get(key);
    }

    public long getLong(String key) {
        Number number = (Number)this.get(key);
        return number != null?number.longValue():0L;
    }

    public JBUser getJBUser(String key) {
        return (JBUser)this.get(key);
    }

    public Date getDate(String key){
        Object o = get(key);
        if (o instanceof Long){
            return new Date((Long) o);
        }else
            return o == null ? null : ((Date) o);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof JBObject && ((JBObject) object).getId().equals(getId());
    }

    public static JBObject createWithoutData(String className, String id) {
        JBObject jbObject = new JBObject(className);
        jbObject.setId(id);
        return jbObject;
    }


    public void saveInBackground(SaveCallback callback) {
        JBCloud.getObjectManager(className).saveObject(this, false, getId(), callback);
    }

    public JBObject save() throws JBException {
        final Object[] objects = new Object[2];
        JBCloud.getObjectManager(className).saveObject(this, true, getId(), new SaveCallback() {
            @Override
            public void done(JBObject object) {
                objects[0] = object;
            }

            @Override
            public void error(JBException e) {
                objects[1] = e;
            }
        });
        if (objects[1] != null)
            throw (JBException) objects[1];
        return (JBObject) objects[0];
    }

    public void deleteInBackground(DeleteCallback callback) {
        deleteByIdInBackground(className , getId() , callback);
    }

    public void delete()throws JBException{
        deleteById(className , getId());
    }

    public static void deleteByIdInBackground(String className, String id, DeleteCallback callback) {
        if (Utils.isBlankString(id))
            return;
        JBCloud.getObjectManager(className).deleteObject(id, false, callback);
    }

    public static void deleteById(String className, String id) throws JBException{
        if (Utils.isBlankString(id))
            return;
        final Object[] objects = new Object[1];
        JBCloud.getObjectManager(className).deleteObject(id, false, new DeleteCallback() {
            @Override
            public void done() {

            }

            @Override
            public void error(JBException e) {
                objects[0] = e;
            }
        });
        if (objects[1] != null)
            throw (JBException) objects[0];
    }

    public void incrementKeyInBackground(String key , RequestCallback callback){
        incrementKeyInBackground(key , 1 , callback);
    }

    public void incrementKeyInBackground(String key , int amount , RequestCallback callback){
        HashMap<String, Integer> map = new HashMap<>();
        map.put(key , amount);
        incrementKeysInBackground(className , getId() , map , callback);
    }

    public void incrementKey(String key) throws JBException {
        incrementKey(key , 1);
    }

    public void incrementKey(String key , int amount) throws JBException {
        HashMap<String, Integer> map = new HashMap<>();
        map.put(key , amount);
        incrementKeys(className , getId() , map);
    }

    public static void incrementKeysInBackground(String className , String id , Map<String , Integer> incrementKeyAmount , RequestCallback callback){
        if (Utils.isBlankString(id))
            return;
        JBCloud.getObjectManager(className).incrementObjectKey(incrementKeyAmount , id , false , callback);
    }

    public static void incrementKeys(String className , String id , Map<String , Integer> incrementKeyAmount) throws JBException{
        if (Utils.isBlankString(id))
            return;
        final Object[] objects = new Object[1];
        JBCloud.getObjectManager(className).incrementObjectKey(incrementKeyAmount, id, true, new RequestCallback() {
            @Override
            public void done() {

            }

            @Override
            public void error(JBException e) {
                objects[0] = e;
            }
        });
        if (objects[0] != null){
            throw (JBException) objects[0];
        }
    }
}
