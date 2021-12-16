package com.example.weather.util;

import android.text.TextUtils;
import android.util.Log;

import com.example.weather.db.City;
import com.example.weather.db.County;
import com.example.weather.db.Province;

import com.example.weather.gson.Weather;
import com.google.gson.Gson;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
//再封装一个工具类，用于反序化Gson数据
public class Utility {

    //反序列化省级数据
    public static boolean handleProvinceResponse(String response){
        //response是传过来的Gson类型的数据
        if (!TextUtils.isEmpty(response)){//如果不是null
            try {
                //创建一个gson数组，因为服务器里的数据就是一个gson数组
                JSONArray allProvince =new JSONArray(response);
                //循环遍历
                for (int i=0;i <allProvince.length();i++){
                    Province province=new Province();//实例化省信息对象
                    //利用JSONArray,反序列化一个个取出来
                    JSONObject provinceObject=allProvince.getJSONObject(i);

                    //返序列化完成后，根据名称将对应值并加入到表中
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));

                    //litepal数据库，可以直接是save()保存数据,将数据保存到省表中
                    province.save();
            }
            return true;
        } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    //解析处理服务器返回来的市级数据
    public static boolean handleCityResponse(String response,int provinceId){//同上

        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allCity =new JSONArray(response);

                for (int i=0;i<allCity.length();i++){
                    City city=new City();//实例化市信息对象

                    JSONObject cityObject=allCity.getJSONObject(i);

                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);

                    city.save();

                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    //解析处理服务器返回来的县级数据
    public static boolean handleCountyResponse(String response,int cityId){//同上

        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allCounty =new JSONArray(response);

                for (int i=0;i<allCounty.length();i++){
                    County county=new County();//实例化市信息对象

                    JSONObject countyObject=allCounty.getJSONObject(i);

                    county.setCountyName(countyObject.getString("name"));
                    //这里是县了，所有要多获取一个天气id
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);

                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static Weather handleWeatherResponse(String response){//将返回的数据解析成Weather实体类
        try {
            //创建JSONObject实例
            JSONObject jsonObject=new JSONObject(response);

            //同前面一样
            JSONArray jsonArray=jsonObject.getJSONArray("HeWeather");

            String weatherContent=jsonArray.getJSONObject(0).toString();

            //利用Gson反序列化,并返回
            return new Gson().fromJson(weatherContent,Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
