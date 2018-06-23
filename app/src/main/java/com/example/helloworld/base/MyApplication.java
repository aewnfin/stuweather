package com.example.helloworld.base;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePal;


public class MyApplication extends Application {

    private static Context context;//获取全局Context 方法getContext() 必须为static
    @Override
    public void onCreate() {
        context=getApplicationContext();
        //初始化数据库
        LitePal.initialize(context);
    }
    public static Context getContext(){
        return context;
    }
}
