package com.example.helloworld.weather.frag;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.helloworld.R;
import com.example.helloworld.weather.gson.Weather;


public class WindFragment extends Fragment {

    public static final int UPDATE_TEXT = 4;

    private TextView windDir;
    private TextView windSc;
    private TextView windSpd;

    private Weather weather;

    private  Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TEXT:
                    windDir.setText(weather.now.windDirection);
                    windSc.setText(weather.now.windEfforts);
                    windSpd.setText(weather.now.windSpeed);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wind, container, false);
        windDir = (TextView) view.findViewById(R.id.wind_dir_text);
        windSc = (TextView) view.findViewById(R.id.wind_sc_text);
        windSpd = (TextView) view.findViewById(R.id.wind_spd_text);
        return view;
    }

    public void reFlash(Weather upWeather) {
        weather = upWeather;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = UPDATE_TEXT;
                handler.sendMessage(message);
            }
        }).start();
    }
}
