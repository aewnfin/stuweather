package com.example.helloworld.base;

import android.util.Log;


public class LogUtil {

    private static final int VERBOSE = 1;
    private static final int DEBUG = 2;
    private static final int INFO = 3;
    private static final int WARN = 4;
    private static final int ERROR = 5;
    private static final int NOTHING = 6;

    private static int level = NOTHING;

    public static void v(String tag,String msg){
        if(level<=VERBOSE){
            Log.d(tag, msg);
        }
    }

    public static void d(String tag,String msg){
        if(level<=DEBUG){
            Log.d(tag, msg);
        }
    }

    public static void i(String tag,String msg){
        if(level<=INFO){
            Log.d(tag, msg);
        }
    }

    public static void w(String tag,String msg){
        if(level<=WARN){
            Log.d(tag, msg);
        }
    }

    public static void e(String tag,String msg){
        if(level<=ERROR){
            Log.d(tag, msg);
        }
    }

}
