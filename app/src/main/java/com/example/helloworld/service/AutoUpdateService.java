package com.example.helloworld.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.example.helloworld.R;
import com.example.helloworld.WeatherActivity;
import com.example.helloworld.base.LogUtil;
import com.example.helloworld.weather.gson.Weather;
import com.example.helloworld.weather.util.HttpUtil;
import com.example.helloworld.weather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public static void serviceStart(Context context){
        Intent intent=new Intent(context,AutoUpdateService.class);
        context.startService(intent);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.d("weather","weather_Update_Service.start");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.d("weather","weather_Update_Service.running");
        updateWeather();
        updateBingPic();
        AlarmManager manager=(AlarmManager)getSystemService(ALARM_SERVICE);
        int anHour=8*60*60*1000;//8小时定时
        long triggerAtTime= SystemClock.elapsedRealtime()+anHour;
        Intent i=new Intent(this,AutoUpdateService.class);
        PendingIntent pi=PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        LogUtil.d("weather","weather_Update_Service.success");
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather(){
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        if (weatherString !=null){
            Weather weather= Utility.handleWeatherResponse(weatherString);
            final String weatherId;
            if(weather==null){
                weatherId="CN101010100";
            }else {
                weatherId=weather.basic.weatherId;
            }
            String weatherUrl="http://guolin.tech/api/weather?cityid=" + weatherId + "&key=8bf22b675e56480993fc36319212265c";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText=response.body().string();
                    Weather weather =Utility.handleWeatherResponse(responseText);
                    if (weather!=null && "ok".equals(weather.status)){
                        SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather",responseText);
                        editor.apply();
                        showNotification(weather);
                    }
                }
            });
        }
    }
    /*
    更新必应每日一图
     */
    private void updateBingPic(){
        String requestBingPic =  "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
            }
        });
    }
    /*
    使用通知
     */
    public void showNotification(Weather weather){
        String weatherNotification=weather.now.more.info+" "+weather.now.temperature+"℃"+" 体感温度"+weather.now.FeelTemperature+"℃";
        Intent intent = new Intent(this, WeatherActivity.class);
        //设置通知 点击 意图
        PendingIntent pi=PendingIntent.getActivity(this,0,intent,0);
        NotificationManager manager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification=new NotificationCompat.Builder(this)
                .setContentTitle(weather.basic.cityName+"未来8小时天气")
                .setContentText(weatherNotification)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_small_sun)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_large_sun))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();
        manager.notify(1,notification);
    }
}
