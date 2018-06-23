package com.example.helloworld.weather.gson;

import com.google.gson.annotations.SerializedName;


public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    @SerializedName("tz")
    public String timeZones;

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime1;
        @SerializedName("utc")
        public String updateTime2;
    }
}
