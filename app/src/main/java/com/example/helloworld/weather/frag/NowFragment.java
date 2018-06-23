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


public class NowFragment extends Fragment {

    public static final int UPDATE_TEXT = 1;

    private TextView degreeText;
    private TextView feelDegreeText;
    private TextView weatherInfoText;

    private Weather weather;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TEXT:
                    String degree = weather.now.temperature + "℃";
                    String feelDegree = "feel " + weather.now.FeelTemperature + "℃";
                    String weatherInfo = weather.now.more.info;
                    degreeText.setText(degree);
                    feelDegreeText.setText(feelDegree);
                    weatherInfoText.setText(weatherInfo);
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.now, container, false);
        degreeText = (TextView) view.findViewById(R.id.degree_text);
        feelDegreeText = (TextView) view.findViewById(R.id.feel_degree_text);
        weatherInfoText = (TextView) view.findViewById(R.id.weather_info_text);
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
