package com.example.helloworld.weather.gson;

import com.google.gson.annotations.SerializedName;


public class Now {
    @SerializedName("tmp")
    public String temperature;//温度
    @SerializedName("fl")
    public String FeelTemperature;//体感温度
    @SerializedName("cond")
    public More more;
    @SerializedName("wind_spd")
    public String windSpeed;//风速
    @SerializedName("wind_sc")
    public String windEfforts;//风力
    @SerializedName("wind_dir")
    public String windDirection;//风向
    public class More{
        @SerializedName("txt")
        public String info;//云量
    }
}
