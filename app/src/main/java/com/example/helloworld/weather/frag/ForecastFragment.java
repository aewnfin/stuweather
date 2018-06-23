package com.example.helloworld.weather.frag;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.helloworld.R;
import com.example.helloworld.weather.gson.Forecast;
import com.example.helloworld.weather.gson.Weather;


public class ForecastFragment extends Fragment {

    public static final int UPDATE_TEXT = 2;

    private LinearLayout forecastLayout;

    private Weather weather;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TEXT:
                    forecastLayout.removeAllViews();
                    for (Forecast forecast : weather.forecastList) {
                        View view = LayoutInflater.from(getActivity()).inflate(R.layout.forecast_item, forecastLayout, false);
                        TextView dateText = (TextView) view.findViewById(R.id.date_text);
                        TextView infoText = (TextView) view.findViewById(R.id.info_text);
                        TextView tepSizeText = (TextView) view.findViewById(R.id.max_min_text);
                        dateText.setText(forecast.date);
                        infoText.setText(forecast.more.info);
                        tepSizeText.setText(forecast.temperature.max + "℃ ~ " + forecast.temperature.min + "℃");
                        forecastLayout.addView(view);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.forecast, container, false);
        forecastLayout = (LinearLayout) view.findViewById(R.id.forecast_layout);
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
