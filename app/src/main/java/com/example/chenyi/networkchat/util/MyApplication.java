package com.example.chenyi.networkchat.util;

import android.app.Application;

/**
 * Created by chenyi on 2017/5/5.
 */

public class MyApplication extends Application {

    private static MyApplication app;

    public static MyApplication getInstance() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }
}
