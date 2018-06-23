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

public class ApiFragment extends Fragment {

    public static final int UPDATE_TEXT = 3;

    private TextView qltyText;
    private TextView apiText;
    private TextView pm25Text;

    private Weather weather;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TEXT:
                    qltyText.setText(weather.aqi.city.qlty);
                    apiText.setText(weather.aqi.city.aqi);
                    pm25Text.setText(weather.aqi.city.pm25);
                    break;
                default:
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.aqi, container, false);
        qltyText = (TextView) view.findViewById(R.id.qlty_text);
        apiText = (TextView) view.findViewById(R.id.api_text);
        pm25Text = (TextView) view.findViewById(R.id.pm25_text);
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
