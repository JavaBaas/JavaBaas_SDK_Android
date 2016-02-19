package com.javabaas.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.util.SparseArray;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import com.javabaas.JBFile;
import com.javabaas.JBObject;

/**
 * Created by xueshukai on 15/9/23 上午11:52.
 */
public class Utils {
    public static Toast toast = null;

    public static void showToast(Context context, String msg) {
        if (toast == null) {
            toast = Toast.makeText(context, msg , Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }

    public static class ViewHolder {
        public static <T extends View> T get(View view, int id) {
            SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
            if (viewHolder == null) {
                viewHolder = new SparseArray<>();
                view.setTag(viewHolder);
            }
            View childView = viewHolder.get(id);
            if (childView == null) {
                childView = view.findViewById(id);
                viewHolder.put(id, childView);
            }
            return (T) childView;
        }
    }

    public static boolean isBlankString(String str) {
        return str == null || str.trim().equals("");
    }

    private static final String dateFormat = "yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'";
    public static final String classNameTag = "className";
    public static final String typeTag = "__type";
    public static final String objectIdTag = "objectId";
    private static Map<Class<?>, Field[]> fieldsMap = Collections.synchronizedMap(new WeakHashMap());
    static Pattern pattern = Pattern.compile("^[a-zA-Z_][a-zA-Z_0-9]*$");
    static Pattern emailPattern = Pattern.compile("^\\w+?@\\w+?[.]\\w+");
    static Pattern phoneNumPattern = Pattern.compile("1\\d{10}");
    static Pattern verifyCodePattern = Pattern.compile("\\d{6}");
    private static final ThreadLocal<SimpleDateFormat> THREAD_LOCAL_DATE_FORMAT = new ThreadLocal();
    static Random random = new Random();
    static AtomicInteger acu = new AtomicInteger(-65536);
    public static final int TYPE_WIFI = 1;
    public static final int TYPE_MOBILE = 2;
    public static final int TYPE_NOT_CONNECTED = 0;
    public static boolean isEmptyList(List e) {
        return e == null || e.isEmpty();
    }

    public static Field[] getAllFiels(Class<?> clazz) {
        if(clazz != null && clazz != Object.class) {
            Field[] theResult = (Field[])fieldsMap.get(clazz);
            if(theResult != null) {
                return theResult;
            } else {
                ArrayList fields = new ArrayList();

                int length;
                for(length = 0; clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
                    Field[] i = clazz.getDeclaredFields();
                    length += i != null?i.length:0;
                    fields.add(i);
                }

                theResult = new Field[length];
                int var11 = 0;
                Iterator i$ = fields.iterator();

                while(true) {
                    Field[] someFields;
                    do {
                        if(!i$.hasNext()) {
                            fieldsMap.put(clazz, theResult);
                            return theResult;
                        }

                        someFields = (Field[])i$.next();
                    } while(someFields == null);

                    Field[] arr$ = someFields;
                    int len$ = someFields.length;

                    for(int i$1 = 0; i$1 < len$; ++i$1) {
                        Field field = arr$[i$1];
                        field.setAccessible(true);
                    }

                    System.arraycopy(someFields, 0, theResult, var11, someFields.length);
                    var11 += someFields.length;
                }
            }
        } else {
            return new Field[0];
        }
    }

    public static boolean checkEmailAddress(String email) {
        return emailPattern.matcher(email).find();
    }

    public static boolean checkMobilePhoneNumber(String phoneNumber) {
        return phoneNumPattern.matcher(phoneNumber).find();
    }

    public static boolean checkMobileVerifyCode(String verifyCode) {
        return verifyCodePattern.matcher(verifyCode).find();
    }

    public static void checkClassName(String className) {
        if(isBlankString(className)) {
            throw new IllegalArgumentException("Blank class name");
        } else if(!pattern.matcher(className).matches()) {
            throw new IllegalArgumentException("Invalid class name");
        }
    }

    public static boolean isBlankContent(String content) {
        return isBlankString(content) || content.trim().equals("{}");
    }

    public static boolean contains(Map<String, Object> map, String key) {
        return map.containsKey(key);
    }

    public static Map<String, Object> createDeleteOpMap(String key) {
        HashMap map = new HashMap();
        map.put("__op", "Delete");
        HashMap result = new HashMap();
        result.put(key, map);
        return result;
    }

    /*public static Map<String, Object> createPointerArrayOpMap(String key, String op, Collection<AVObject> objects) {
        HashMap map = new HashMap();
        map.put("__op", op);
        ArrayList list = new ArrayList();
        Iterator result = objects.iterator();

        while(result.hasNext()) {
            AVObject obj = (AVObject)result.next();
            list.add(mapFromPointerObject(obj));
        }

        map.put("objects", list);
        HashMap result1 = new HashMap();
        result1.put(key, map);
        return result1;
    }*/

    public static Map<String, Object> createStringObjectMap(String key, Object value) {
        HashMap map = new HashMap();
        map.put(key, value);
        return map;
    }

    public static Map<String, Object> mapFromPointerObject(JBObject object) {
        HashMap result = new LinkedHashMap();
        result.put("__type", "Pointer");
        result.put("className", object.getClassName());
        if(!isBlankString(object.getId())) {
            result.put("_id", object.getId());
        }
        return result;
    }

    public static Map<String, Object> mapFromFileObject(JBFile object) {
        HashMap result = new LinkedHashMap();
        result.put("__type", "File");
        if(!isBlankString(object.getId())) {
            result.put("_id", object.getId());
        }
        return result;
    }

    public static Map<String, Object> mapFromUserObjectId(String userObjectId) {
        if(isBlankString(userObjectId)) {
            return null;
        } else {
            HashMap result = new HashMap();
            result.put("__type", "Pointer");
            result.put("className", "_User");
            result.put("_id", userObjectId);
            return result;
        }
    }

    public static boolean isDigitString(String s) {
        if(s == null) {
            return false;
        } else {
            for(int i = 0; i < s.length(); ++i) {
                char c = s.charAt(i);
                if(!Character.isDigit(c)) {
                    return false;
                }
            }

            return true;
        }
    }

    public static Date dateFromString(String content) {
        if(isBlankString(content)) {
            return null;
        } else if(isDigitString(content)) {
            return new Date(Long.parseLong(content));
        } else {
            Date date = null;
            SimpleDateFormat format = (SimpleDateFormat)THREAD_LOCAL_DATE_FORMAT.get();
            if(format == null) {
                format = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'");
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                THREAD_LOCAL_DATE_FORMAT.set(format);
            }

            try {
                date = format.parse(content);
            } catch (Exception var4) {
                //log.e(var4.toString());
            }

            return date;
        }
    }

    public static String stringFromDate(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String isoDate = df.format(date);
        return isoDate;
    }

    public static long mapFromDate(Date date) {
        HashMap result = new HashMap();
        result.put("__type", "Date");
        result.put("iso", stringFromDate(date));
        return date.getTime();
        //return result;
    }

    public static Date dateFromMap(Map<String, Object> map) {
        String value = (String)map.get("iso");
        return dateFromString(value);
    }

    /*public static Map<String, Object> mapFromGeoPoint(AVGeoPoint point) {
        HashMap result = new HashMap();
        result.put("__type", "GeoPoint");
        result.put("latitude", Double.valueOf(point.getLatitude()));
        result.put("longitude", Double.valueOf(point.getLongitude()));
        return result;
    }

    public static AVGeoPoint geoPointFromMap(Map<String, Object> map) {
        double la = ((Number)map.get("latitude")).doubleValue();
        double lo = ((Number)map.get("longitude")).doubleValue();
        AVGeoPoint point = new AVGeoPoint(la, lo);
        return point;
    }

    public static AVObject objectFromRelationMap(Map<String, Object> map) {
        String className = (String)map.get("className");
        AVObject object = objectFromClassName(className);
        return object;
    }*/

    public static Map<String, Object> mapFromByteArray(byte[] data) {
        HashMap result = new HashMap();
        result.put("__type", "Bytes");
        result.put("base64", Base64.encodeToString(data, 2));
        return result;
    }

    public static byte[] dataFromMap(Map<String, Object> map) {
        String value = (String)map.get("base64");
        return Base64.decode(value, 2);
    }


    /*public static String jsonStringFromMapWithNull(Object map) {
        return AVOSCloud.showInternalDebugLog()? JSON.toJSONString(map, new SerializerFeature[]{SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullBooleanAsFalse, SerializerFeature.WriteNullNumberAsZero, SerializerFeature.PrettyFormat}):JSON.toJSONString(map, new SerializerFeature[]{SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullBooleanAsFalse, SerializerFeature.WriteNullNumberAsZero});
    }

    public static String jsonStringFromObjectWithNull(Object map) {
        return AVOSCloud.showInternalDebugLog()?JSON.toJSONString(map, new SerializerFeature[]{SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullBooleanAsFalse, SerializerFeature.WriteNullNumberAsZero, SerializerFeature.PrettyFormat}):JSON.toJSONString(map, new SerializerFeature[]{SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullBooleanAsFalse, SerializerFeature.WriteNullNumberAsZero});
    }

    public static Map<String, Object> mapFromFile(AVFile file) {
        HashMap result = new HashMap();
        result.put("__type", AVFile.className());
        result.put("metaData", file.getMetaData());
        switch(AVUtils.SyntheticClass_1.$SwitchMap$com$avos$avoscloud$AVOSCloud$StorageType[AVOSCloud.getStorageType().ordinal()]) {
            case 1:
                result.put("name", file.getName());
                break;
            case 2:
            case 3:
                result.put("id", file.getName());
        }

        return result;
    }

    public static AVFile fileFromMap(Map<String, Object> map) {
        AVFile file = new AVFile("", "");
        copyPropertiesFromMapToObject(map, file);
        Object metadata = map.get("metaData");
        if(metadata != null && metadata instanceof Map) {
            file.getMetaData().putAll((Map)metadata);
        }

        if(isBlankString((String)file.getMetaData("_name"))) {
            file.getMetaData().put("_name", file.getName());
        }

        switch(AVUtils.SyntheticClass_1.$SwitchMap$com$avos$avoscloud$AVOSCloud$StorageType[AVOSCloud.getStorageType().ordinal()]) {
            case 2:
            case 3:
                file.setName((String)map.get("objectId"));
            case 1:
            default:
                return file;
        }
    }

    public static AVObject parseObjectFromMap(Map<String, Object> map) {
        AVObject object = newAVObjectByClassName((String)map.get("className"));
        object.setObjectId((String)map.get("objectId"));
        copyPropertiesFromMapToAVObject(map, object);
        return object;
    }

    public static String restfulServerData(Map<String, ?> data) {
        if(data == null) {
            return "{}";
        } else {
            Map map = getParsedMap(data);
            return jsonStringFromMapWithNull(map);
        }
    }
    */
    public static boolean hasProperty(Class<?> clazz, String property) {
        Field[] fields = getAllFiels(clazz);
        Field[] arr$ = fields;
        int len$ = fields.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            Field f = arr$[i$];
            if(f.getName().equals(property)) {
                return true;
            }
        }

        return false;
    }

    public static boolean checkAndSetValue(Class<?> clazz, Object parent, String property, Object value) {
        if(clazz == null) {
            return false;
        } else {
            try {
                Field[] exception = getAllFiels(clazz);
                Field[] arr$ = exception;
                int len$ = exception.length;

                for(int i$ = 0; i$ < len$; ++i$) {
                    Field f = arr$[i$];
                    if(f.getName().equals(property) && (f.getType().isInstance(value) || value == null)) {
                        f.set(parent, value);
                        return true;
                    }
                }

                return false;
            } catch (Exception var9) {
                return false;
            }
        }
    }

    /*public static String getEncodeUrl(String url, Map<String, String> params) {
        return AsyncHttpClient.getUrlWithQueryString(true, url, new RequestParams(params));
    }*/

    public static String getJSONValue(String msg, String key) {
        Map jsonMap = (Map)JSON.parseObject(msg, HashMap.class);
        if(jsonMap != null && !jsonMap.isEmpty()) {
            Object action = jsonMap.get(key);
            return action != null?action.toString():null;
        } else {
            return null;
        }
    }

    public static String jsonStringFromMapWithNull(Object map) {
        return JSON.toJSONString(map, new SerializerFeature[]{SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullBooleanAsFalse, SerializerFeature.WriteNullNumberAsZero});
    }

    public static String jsonStringFromObjectWithNull(Object map) {
        return JSON.toJSONString(map, new SerializerFeature[]{SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullBooleanAsFalse, SerializerFeature.WriteNullNumberAsZero});
    }

    public static String restfulServerData(Map<String, Object> data) {
        if(data == null) {
            return "{}";
        } else {
            Map map = getParsedMap(data);
            return jsonStringFromMapWithNull(map);
        }
    }

    public static Map<String, Object> getParsedMap(Map<String, Object> map) {
        LinkedHashMap newMap = new LinkedHashMap<>(map.size());
        Iterator i$ = map.entrySet().iterator();

        while(i$.hasNext()) {
            Map.Entry entry = (Map.Entry)i$.next();
            String key = (String)entry.getKey();
            Object o = entry.getValue();
            newMap.put(key, getParsedObject(o));
        }

        return newMap;
    }

    public static Object getParsedObject(Object object) {
        return object == null?null:(object instanceof JBObject? mapFromPointerObject((JBObject) object):(object instanceof Map ?getParsedMap((Map) object):(object instanceof Collection ?getParsedList((Collection) object):(object instanceof Date ?mapFromDate((Date) object):(object instanceof byte[]?mapFromByteArray((byte[])((byte[])object)):((object instanceof JSONObject ?JSON.parse(object.toString()):(object instanceof JSONArray ?JSON.parse(object.toString()):object))))))));
    }

    static List getParsedList(Collection list) {
        ArrayList newList = new ArrayList(list.size());
        Iterator i$ = list.iterator();

        while(i$.hasNext()) {
            Object o = i$.next();
            newList.add(getParsedObject(o));
        }

        return newList;
    }

    public static String joinCollection(Collection<String> collection, String separator) {
        StringBuilder builder = new StringBuilder();
        boolean wasFirst = true;
        Iterator i$ = collection.iterator();

        while(i$.hasNext()) {
            String value = (String)i$.next();
            if(wasFirst) {
                wasFirst = false;
                builder.append(value);
            } else {
                builder.append(separator).append(value);
            }
        }

        return builder.toString();
    }

    public static boolean isWifi(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetInfo != null && activeNetInfo.getType() == 1;
    }

    public static String stringFromBytes(byte[] bytes) {
        try {
            return new String(bytes, "UTF-8");
        } catch (Exception var2) {
            return null;
        }
    }

    public static String Base64Encode(String data) {
        return Base64.encodeToString(data.getBytes(), 10);
    }

    public static String getRandomString(int length) {
        String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder randomString = new StringBuilder(length);

        for(int i = 0; i < length; ++i) {
            randomString.append(letters.charAt(random.nextInt(letters.length())));
        }

        return randomString.toString();
    }

    public static Map<String, Object> createMap(String cmp, Object value) {
        HashMap dict = new HashMap();
        dict.put(cmp, value);
        return dict;
    }

    public static Map<String, Object> bean2Map(Object javaBean) {
        Map<String, Object> ret = new LinkedHashMap<>();
        try {
            Method[] methods = javaBean.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().startsWith("get")) {
                    String field = method.getName();
                    field = field.substring(field.indexOf("get") + 3);
                    field = field.toLowerCase().charAt(0) + field.substring(1);
                    Object value = method.invoke(javaBean, (Object[]) null);
                    ret.put(field, (null == value ? "" : value));
                }
            }
        } catch (Exception e) {
        }
        return ret;
    }

    public static String MD5(String s) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str).toLowerCase();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

}
