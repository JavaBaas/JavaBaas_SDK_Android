package com.javabaas.sample;

import android.app.Application;

import com.javabaas.callback.GetInstallationIdCallback;
import com.javabaas.exception.JBException;
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
        JBCloud.showLog();
        JBCloud.init(this, "a8c18441d7ab4dcd9ed78477015ab8b2", "594895b0b55198292ae266f1", "http://192.168.1.39:9000", new GetInstallationIdCallback() {
            @Override
            public void done(String id) {
                System.out.println(id);
            }

            @Override
            public void error(JBException e) {
                System.out.println(e);
            }
        });
    }
}
