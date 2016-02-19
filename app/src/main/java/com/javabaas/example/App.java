package com.javabaas.example;

import android.app.Application;

import com.orhanobut.logger.Logger;

import com.javabaas.JBCloud;

/**
 * Created by xueshukai on 15/12/18 下午4:57.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Logger.init("JavaBaasLog").setMethodCount(0).hideThreadInfo();
        JBCloud.init(this , "1f7049bfde7d440cb31210aa5e4d44ed" , "5645b2a574242e39eee89829" , "https://api.javabaas.com");
    }
}
