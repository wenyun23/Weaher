package com.example.weather.gson;

import com.google.gson.annotations.SerializedName;

//daily_forecast是一个数组，包含未来一周的天气信息
public class Forecast {
    public String date;

    @SerializedName("cond")
    public More more;

    public static class More{
      @SerializedName("txt_d")
      public String info;
    }

    @SerializedName("tmp")
    public Temperature temperature;

    public static class Temperature{
        public String max;
        public String min;
    }
}

