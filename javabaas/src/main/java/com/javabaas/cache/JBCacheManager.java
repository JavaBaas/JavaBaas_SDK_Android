package com.javabaas.cache;

import com.alibaba.fastjson.JSON;

import java.io.File;
import java.util.List;

import com.javabaas.IObjectManager;
import com.javabaas.JBCloud;
import com.javabaas.callback.FindCallback;
import com.javabaas.JBObject;
import com.javabaas.exception.JBException;
import com.javabaas.util.Utils;

public class JBCacheManager {

    private static JBCacheManager instance = null;

    private static File keyValueCacheDir() {
        File dir = new File(JBPersistenceUtils.getCacheDir(), "JBKeyValueCache");
        dir.mkdirs();
        return dir;
    }

    private static File getCacheFile(String fileName) {
        return new File(keyValueCacheDir(), fileName);
    }

    private JBCacheManager() {
    }

    public static synchronized JBCacheManager sharedInstance() {
        if(instance == null) {
            instance = new JBCacheManager();
        }
        return instance;
    }

    public String fileCacheKey(String key, String ts) {
        return !Utils.isBlankString(ts)?Utils.MD5(key + ts):Utils.MD5(key);
    }

    public boolean hasCache(String key) {
        return this.hasCache(key, (String)null);
    }

    public boolean hasCache(String key, String ts) {
        File file = this.getCacheFile(key, ts);
        return file.exists();
    }

    public boolean hasValidCache(String key, String ts, long maxAgeInMilliseconds) {
        File file = this.getCacheFile(key, ts);
        return file.exists() && System.currentTimeMillis() - file.lastModified() < maxAgeInMilliseconds;
    }

    private File getCacheFile(String key, String ts) {
        return getCacheFile(this.fileCacheKey(key, ts));
    }

    public <T> void get(String key, long maxAgeInMilliseconds, String className, Class<T> clazz, FindCallback<T> getCallback) {
        File file = this.getCacheFile(key, className);
        if(file.exists() && (maxAgeInMilliseconds <= 0L || System.currentTimeMillis() - file.lastModified() <= maxAgeInMilliseconds)) {
            String content = JBPersistenceUtils.readContentFromFile(file);
            List<T> ts1 = JSON.parseArray(content, clazz);
            if (clazz.isInstance(new JBObject())){
                for (T t : ts1) {
                    JBObject jbObject = (JBObject) t;
                    jbObject.setClassName(className);
                    IObjectManager.parseJBObject(jbObject);
                }
            }
            getCallback.done(ts1);
        } else {
            getCallback.error(new JBException("cache miss"));
        }

    }

    public void delete(String key) {
        File file = getCacheFile(Utils.MD5(key));
        String absolutePath = file.getAbsolutePath();
        if(file.exists()) {
            if(!file.delete()) {
                JBPersistenceUtils.saveContentToFile("{}", file);
            } else {
                JBPersistenceUtils.removeLock(absolutePath);
            }
        }

    }

    public boolean save(String key, String content, String lastModifyTs) {
        File cacheFile = this.getCacheFile(key, lastModifyTs);
        return JBPersistenceUtils.saveContentToFile(content, cacheFile);
    }

    public String get(String key){
        File file = this.getCacheFile(key, null);
        return JBPersistenceUtils.readContentFromFile(file);
    }

    public void remove(String key, String ts) {
        File cacheFile = this.getCacheFile(key, ts);
        String absolutePath = cacheFile.getAbsolutePath();
        if(cacheFile.exists()) {
            if(!cacheFile.delete()) {
                JBPersistenceUtils.saveContentToFile("{}", cacheFile);
            } else {
                JBPersistenceUtils.removeLock(absolutePath);
            }
        }

    }

    public boolean haveCache(String key) {
        return getCacheFile(Utils.MD5(key)).exists();
    }

    public static boolean clearAllCache() {
        return clearCacheMoreThanDays(-1);
    }

    public static boolean clearCacheMoreThanOneDay() {
        return clearCacheMoreThanDays(1);
    }

    public static boolean clearCacheMoreThanDays(int numberOfDays) {
        File keyValueCacheDir = keyValueCacheDir();
        File[] cacheFiles = keyValueCacheDir.listFiles();
        if(cacheFiles != null) {
            File[] arr$ = cacheFiles;
            int len$ = cacheFiles.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                File file = arr$[i$];
                if(System.currentTimeMillis() - file.lastModified() > (long)numberOfDays * 24L * 3600L * 1000L && file.exists()) {
                    String path = file.getAbsolutePath();
                    if(!file.delete()) {
                        return false;
                    }

                    JBPersistenceUtils.removeLock(path);
                }
            }
        } else {
        }

        return true;
    }

    public static boolean clearFileCacheMoreThanDays(int numberOfDays) {
        if(JBCloud.applicationContext == null) {
            return false;
        } else {
            File keyValueCacheDir = JBCloud.applicationContext.getFilesDir();
            File[] cacheFiles = keyValueCacheDir.listFiles();
            if(cacheFiles != null) {
                File[] arr$ = cacheFiles;
                int len$ = cacheFiles.length;

                for(int i$ = 0; i$ < len$; ++i$) {
                    File file = arr$[i$];
                    if(System.currentTimeMillis() - file.lastModified() > (long)numberOfDays * 24L * 3600L * 1000L && file.exists() && file.isFile()) {
                        String path = file.getAbsolutePath();
                        if(!file.delete()) {
                            return false;
                        }

                        JBPersistenceUtils.removeLock(path);
                    }
                }
            } else {
            }

            return true;
        }
    }
}
