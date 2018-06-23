package com.example.helloworld;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.example.helloworld.base.BaseActivity;

public class StartActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        //获取缓存
        String choose = getIntent().getStringExtra("choose");
        if (choose == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String weather = prefs.getString("weather", null);
            if (weather != null) {
                Intent intent = new Intent(this, WeatherActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}
