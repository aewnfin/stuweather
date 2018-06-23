package com.example.helloworld.base;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

public class ActivityCollector {
    private static List<Activity> activities =new ArrayList<>();

    static void addActivity(Activity activity){
        activities.add(activity);
    }
    static void removeActivity(Activity activity){
        activities.remove(activity);
    }
    public static void finishAll(){
        for (Activity activity:activities){
            if (!activity.isFinishing()){
                activity.finish();
            }
        }
        activities.clear();
    }
}
