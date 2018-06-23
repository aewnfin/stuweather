package com.example.helloworld.weather.gson;


public class AQI {

    public AQICity city;

    public class AQICity{//需为public 否则无法被json解析引用
        public String aqi;
        public String pm25;
        public String qlty;
    }
}
