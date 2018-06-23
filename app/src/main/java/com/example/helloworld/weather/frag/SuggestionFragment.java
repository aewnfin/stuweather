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


public class SuggestionFragment extends Fragment {

    public static final int UPDATE_TEXT = 5;

    private TextView comfortEva;
    private TextView comfortText;

    private TextView carWashEva;
    private TextView carWashText;

    private TextView sportEva;
    private TextView sportText;

    private Weather weather;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TEXT:
                    String comfortTitle = "舒适度：" + weather.suggestion.comfort.Evaluation;
                    String carWashTitle = "洗车指数：" + weather.suggestion.carWash.Evaluation;
                    String sportTitle = "运动建议：" + weather.suggestion.sport.Evaluation;
                    comfortEva.setText(comfortTitle);
                    carWashEva.setText(carWashTitle);
                    sportEva.setText(sportTitle);
                    String comfort = weather.suggestion.comfort.info;
                    String carWash = weather.suggestion.carWash.info;
                    String sport = weather.suggestion.sport.info;
                    comfortText.setText(comfort);
                    carWashText.setText(carWash);
                    sportText.setText(sport);
                    break;
                default:
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.suggestion, container, false);
        comfortEva = (TextView) view.findViewById(R.id.comfort_Eva);
        comfortText = (TextView) view.findViewById(R.id.comfort_text);
        carWashEva = (TextView) view.findViewById(R.id.car_wash_Eva);
        carWashText = (TextView) view.findViewById(R.id.car_wash_text);
        sportEva = (TextView) view.findViewById(R.id.sport_Eva);
        sportText = (TextView) view.findViewById(R.id.sport_text);
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
