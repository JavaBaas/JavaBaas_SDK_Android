package com.javabaas.cache;

import android.content.SharedPreferences;


import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.javabaas.JBCloud;
import com.javabaas.util.Utils;

public class JBPersistenceUtils {


    private static JBPersistenceUtils instance = null;
    private static ConcurrentHashMap<String, ReentrantReadWriteLock> fileLocks = new ConcurrentHashMap();

    private static ReentrantReadWriteLock getLock(String path) {
        ReentrantReadWriteLock lock = (ReentrantReadWriteLock)fileLocks.get(path);
        if(lock == null) {
            lock = new ReentrantReadWriteLock();
            ReentrantReadWriteLock oldLock = (ReentrantReadWriteLock)fileLocks.putIfAbsent(path, lock);
            if(oldLock != null) {
                lock = oldLock;
            }
        }

        return lock;
    }

    public static void removeLock(String path) {
        fileLocks.remove(path);
    }

    private JBPersistenceUtils() {
    }

    public static synchronized JBPersistenceUtils sharedInstance() {
        if(instance == null) {
            instance = new JBPersistenceUtils();
        }

        return instance;
    }

    public static File getPaasDocumentDir() {
        if(JBCloud.applicationContext == null) {
            throw new IllegalStateException("applicationContext is null, Please call JBCloud.init first");
        } else {
            return JBCloud.applicationContext.getDir("Baas", 0);
        }
    }

    public static File getCacheDir() {
        if(JBCloud.applicationContext == null) {
            throw new IllegalStateException("applicationContext is null, Please call JBCloud.init first");
        } else {
            return JBCloud.applicationContext.getCacheDir();
        }
    }

    public static File getCommandCacheDir() {
        if(JBCloud.applicationContext == null) {
            throw new IllegalStateException("applicationContext is null, Please call JBCloud.init first");
        } else {
            File dir = new File(getCacheDir(), "CommandCache");
            dir.mkdirs();
            return dir;
        }
    }

    public static File getAnalysisCacheDir() {
        if(JBCloud.applicationContext == null) {
            throw new IllegalStateException("applicationContext is null, Please call JBCloud.init first");
        } else {
            File dir = new File(getCacheDir(), "Analysis");
            dir.mkdirs();
            return dir;
        }
    }

    public static void closeQuietly(Closeable closeable) {
        try {
            if(closeable != null) {
                closeable.close();
            }
        } catch (IOException var2) {
            //log.d(var2.toString());
        }

    }

    private static File getFile(String folderName, String fileName) {
        File file;
        if(Utils.isBlankString(folderName)) {
            file = new File(getPaasDocumentDir(), fileName);
        } else {
            File folder = new File(getPaasDocumentDir(), folderName);
            if(!folder.exists()) {
                folder.mkdirs();
            }

            file = new File(folder, fileName);
        }

        return file;
    }

    public void saveToDocumentDir(String content, String fileName) {
        this.saveToDocumentDir(content, (String)null, fileName);
    }

    public void saveToDocumentDir(String content, String folderName, String fileName) {
        File fileForSave = getFile(folderName, fileName);
        saveContentToFile(content, fileForSave);
    }

    public static boolean saveContentToFile(String content, File fileForSave) {
        try {
            return saveContentToFile(content.getBytes("utf-8"), fileForSave);
        } catch (UnsupportedEncodingException var3) {
            //log.d(var3.toString());
            return false;
        }
    }

    public static boolean saveContentToFile(byte[] content, File fileForSave) {
        ReentrantReadWriteLock.WriteLock writeLock = getLock(fileForSave.getAbsolutePath()).writeLock();
        boolean succeed = true;
        FileOutputStream out = null;
        if(writeLock.tryLock()) {
            try {
                out = new FileOutputStream(fileForSave, false);
                out.write(content);
            } catch (Exception var9) {
                //log.d(var9.toString());
                succeed = false;
            } finally {
                if(out != null) {
                    closeQuietly(out);
                }

                writeLock.unlock();
            }
        }

        return succeed;
    }

    public String getFromDocumentDir(String folderName, String fileName) {
        File fileForRead = getFile(folderName, fileName);
        return readContentFromFile(fileForRead);
    }

    public String getFromDocumentDir(String fileName) {
        return this.getFromDocumentDir((String)null, fileName);
    }

    public static String readContentFromFile(File fileForRead) {
        byte[] data = readContentBytesFromFile(fileForRead);
        return data != null && data.length != 0?new String(data):"";
    }

    public static byte[] readContentBytesFromFile(File fileForRead) {
        if(fileForRead == null) {
            return null;
        } else if(fileForRead.exists() && fileForRead.isFile()) {
            ReentrantReadWriteLock.ReadLock readLock = getLock(fileForRead.getAbsolutePath()).readLock();
            readLock.lock();
            Object data = null;
            BufferedInputStream input = null;

            try {
                byte[] data1 = new byte[(int)fileForRead.length()];
                int e = 0;
                input = new BufferedInputStream(new FileInputStream(fileForRead), 8192);

                while(e < data1.length) {
                    int bytesRemaining = data1.length - e;
                    int bytesRead = input.read(data1, e, bytesRemaining);
                    if(bytesRead > 0) {
                        e += bytesRead;
                    }
                }

                byte[] bytesRemaining1 = data1;
                return bytesRemaining1;
            } catch (IOException var10) {
            } finally {
                closeQuietly(input);
                readLock.unlock();
            }

            return null;
        } else {
            return null;
        }
    }

    public void savePersistentSettingBoolean(String keyzone, String key, Boolean value) {
        if(JBCloud.applicationContext == null) {
        } else {
            SharedPreferences settings = JBCloud.applicationContext.getSharedPreferences(keyzone, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(key, value.booleanValue());
            editor.apply();
        }
    }

    public boolean getPersistentSettingBoolean(String keyzone, String key) {
        return this.getPersistentSettingBoolean(keyzone, key, Boolean.valueOf(false));
    }

    public boolean getPersistentSettingBoolean(String keyzone, String key, Boolean defaultValue) {
        if(JBCloud.applicationContext == null) {
            return defaultValue.booleanValue();
        } else {
            SharedPreferences settings = JBCloud.applicationContext.getSharedPreferences(keyzone, 0);
            return settings.getBoolean(key, defaultValue.booleanValue());
        }
    }

    public void savePersistentSettingInteger(String keyzone, String key, Integer value) {
        if(JBCloud.applicationContext == null) {
        } else {
            SharedPreferences settings = JBCloud.applicationContext.getSharedPreferences(keyzone, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(key, value.intValue());
            editor.apply();
        }
    }

    public Integer getPersistentSettingInteger(String keyzone, String key, Integer defaultValue) {
        if(JBCloud.applicationContext == null) {
            return defaultValue;
        } else {
            SharedPreferences settings = JBCloud.applicationContext.getSharedPreferences(keyzone, 0);
            return Integer.valueOf(settings.getInt(key, defaultValue.intValue()));
        }
    }

    public void savePersistentSettingString(String keyzone, String key, String value) {
        if(JBCloud.applicationContext == null) {
        } else {
            SharedPreferences settings = JBCloud.applicationContext.getSharedPreferences(keyzone, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(key, value);
            editor.apply();
        }
    }

    public String getPersistentSettingString(String keyzone, String key, String defaultValue) {
        if(JBCloud.applicationContext == null) {
            return defaultValue;
        } else {
            SharedPreferences settings = JBCloud.applicationContext.getSharedPreferences(keyzone, 0);
            return settings.getString(key, defaultValue);
        }
    }

    public void removePersistentSettingString(String keyzone, String key) {
        SharedPreferences settings = JBCloud.applicationContext.getSharedPreferences(keyzone, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(key);
        editor.apply();
    }

    public String removePersistentSettingString(String keyzone, String key, String defaultValue) {
        String currentValue = this.getPersistentSettingString(keyzone, key, defaultValue);
        SharedPreferences settings = JBCloud.applicationContext.getSharedPreferences(keyzone, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(key);
        editor.apply();
        return currentValue;
    }

    public void removeKeyZonePersistentSettings(String keyzone) {
        SharedPreferences settings = JBCloud.applicationContext.getSharedPreferences(keyzone, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.apply();
    }
}
