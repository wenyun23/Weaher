package com.example.weather.gson;

import com.google.gson.annotations.SerializedName;

//当前温度和当前天气类别
public class Now {
    @SerializedName("tmp")
    public String temperature;//温度

    @SerializedName("cond")
    public More more;//天气类别

    public static class More{
        @SerializedName("txt")
        public String info;
    }
}
