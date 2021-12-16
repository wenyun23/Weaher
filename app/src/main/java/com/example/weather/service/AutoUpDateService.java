package com.example.weather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.weather.gson.Weather;
import com.example.weather.util.HttpUtil;
import com.example.weather.util.Utility;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
//使后台服务Service,每四小时天气自动刷新,(注:Service的生命周期与Activity不一样)
public class AutoUpDateService extends Service {



    //重写onStartCommand方法
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();//刷新天气
        updateBingPic();//刷新背景图

        //定义一个闹钟服务,并实例化对象
        AlarmManager clock= (AlarmManager) getSystemService(ALARM_SERVICE);

        int anHour=60*60*1000;//1000就是秒,60*1000就是60秒等于一分钟,60*60*1000=1h

        //SystemClock.elapsedRealtime()返回系统启动到现在的时间
        long triggerAtTimer = SystemClock.elapsedRealtime()+anHour;
        //利用Intent启动服务
        Intent intent1=new Intent(this,AutoUpDateService.class);

        //PendingIntent可以通过getService方法从系统取得一个用于启动一个Service的PendingIntent对象.
        PendingIntent pi=PendingIntent.getService(this,0,intent1,0);

        //取消AlarmManager的定时服务
        clock.cancel(pi);

        //设置一次性闹钟
        clock.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTimer,pi);

        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather(){//更新天气,基本和前面的是一样的

         SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
         String weatherString=prefs.getString("weather",null);

         if (weatherString!=null){//如果有缓存，就直接解析数据,这肯定是有缓存的，不存在没有缓存
             Weather weather= Utility.handleWeatherResponse(weatherString);
             assert weather != null;//判断是否为空
             String weatherId=weather.basic.weatherId;

             String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+
                     "&key=0b675b7e86ff4294ad89692a771fe09d";
             //下面就前面的一样了,根据网址去服务器请求数据
             HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                 @Override
                 public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                     final String responseText= Objects.requireNonNull(response.body()).string();
                     if ("ok".equals(weather.status)){
                         //再次缓存天气
                         SharedPreferences.Editor editor=PreferenceManager.
                                 getDefaultSharedPreferences(AutoUpDateService.this).edit();
                         editor.putString("weather",responseText);
                         editor.apply();
                     }
                 }
                 @Override
                 public void onFailure(@NonNull Call call, @NonNull IOException e) {
                     e.printStackTrace();
                 }
             });
         }

    }

    private void updateBingPic(){//更新背景图

        String requestBingPic="http://guolin.tech/api/bing_pic";

        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {//同前面一样
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String binPic=response.body().string();
                //缓存图片,原来和天气界面一样的
                SharedPreferences.Editor editor= PreferenceManager.
                        getDefaultSharedPreferences(AutoUpDateService.this).edit();
                editor.putString("bing_pic",binPic);
                editor.apply();
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {//该方法必须重写
        return null;
    }
}
