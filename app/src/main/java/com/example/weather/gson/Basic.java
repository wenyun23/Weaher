package com.example.weather.gson;

import com.google.gson.annotations.SerializedName;

//一个城市基本天气信息
public class Basic {
    //@SerializedName注解的方式来定义名字
    @SerializedName("city")
    public String cityName;//城市名称

    @SerializedName("id")
    public String weatherId;//城市对应天气的id

    public Update update;//loc更新时间

    public static class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
