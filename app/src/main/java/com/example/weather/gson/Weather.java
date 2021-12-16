package com.example.weather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

//该类用于整合前面的所有关于天气信息的类，Forecast由于是个数组，所有这里用集合引用
public class Weather {
    public String status;//天气还会返回包含一项status数据,成功返回OK。失败则返回具体原因
    public AQI aqi;
    public Basic basic;
    public Now now;
    public  Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> mForecastList;
}
