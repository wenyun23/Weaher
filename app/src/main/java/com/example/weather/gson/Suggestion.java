package com.example.weather.gson;

import com.google.gson.annotations.SerializedName;

//comf表示气体体感,cw表示洗车建议,sport表示运动建议
public class Suggestion {

    @SerializedName("comf")
    public Comfort comfort;

    @SerializedName("cw")
    public CarWash carWash;

    public Sport sport;

    public static class Comfort{
        @SerializedName("txt")
        public String info;
    }
    public static class CarWash{
        @SerializedName("txt")
        public String info;
    }

    public static class Sport{
        @SerializedName("txt")
        public String info;
    }
}
