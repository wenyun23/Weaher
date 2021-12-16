package com.example.weather.gson;

//天气中AQI空气质量
public class AQI {

    public AQICity city;
    public static class AQICity{
        public String aqi;
        public String pm25;
    }
}
