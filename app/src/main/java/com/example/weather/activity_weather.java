package com.example.weather;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.weather.gson.Forecast;
import com.example.weather.gson.Weather;
import com.example.weather.service.AutoUpDateService;
import com.example.weather.util.HttpUtil;
import com.example.weather.util.Utility;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class activity_weather extends AppCompatActivity {
    private ScrollView scrollview;
    private TextView titleCity,titleUpdateTime,degreeText,weatherInfoText;
    private TextView aqiText,pm25Text,comfortText,carWashText,sportText;
    private LinearLayout linearLayout;

    private ImageView bingPicImg;
    public SwipeRefreshLayout swipeRefresh;

    public DrawerLayout drawerLayout;
    private Button nacButton;

    public String weatherId;//定义全局变量存天气id,用于下面下拉刷新
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        initView();//初始化组件
        listener();//重写监听事件和其他
    }

    public void initView(){
        drawerLayout=findViewById(R.id.drawer_layout);
        scrollview=findViewById(R.id.scrollview);
        nacButton=findViewById(R.id.btn_nav);
        swipeRefresh=findViewById(R.id.swipe_refresh);
        bingPicImg=findViewById(R.id.bin_pic_img);

        titleCity=findViewById(R.id.title_city);
        titleUpdateTime=findViewById(R.id.title_update_time);

        degreeText=findViewById(R.id.degree_text);//"温度"
        weatherInfoText=findViewById(R.id.weather_info_text);//天气状态,"多云"

        linearLayout=findViewById(R.id.linearLayout);//"预报"

        aqiText=findViewById(R.id.aqi_text);
        pm25Text=findViewById(R.id.pm25_text);

        comfortText=findViewById(R.id.comfort_text);
        carWashText=findViewById(R.id.car_wash_text);
        sportText=findViewById(R.id.sport_text);

    }

    @SuppressLint("ObsoleteSdkInt")
    public void listener(){

        //判断一下sdk是否大于21，也就是安卓5以上，下面代码是使背景和显示的东西完全融合，视觉感觉是融合更快
        if (Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();

            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }


        //为左上角按钮设置监听事件
        nacButton.setOnClickListener(view -> {
            drawerLayout.openDrawer(GravityCompat.START);//打开抽屉布局,START表示从左
        });


        //设置下拉刷新"图标"颜色
        swipeRefresh.setColorSchemeResources(R.color.design_default_color_primary);

        swipeRefresh.setOnRefreshListener(() -> {
            requestWeather(weatherId);//下拉刷新就去重新请求一次数据
        });

        //利用SharedPreferences缓存少量数据
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);

        //从缓存中找数据
        String weatherString=prefs.getString("weather",null);

        if (weatherString!=null){//如果缓存有,就直接调用工具类,去解析天气数据
            Weather weather= Utility.handleWeatherResponse(weatherString);//先反序列化
            assert weather != null;//判断不为空

            weatherId=weather.basic.weatherId;
            showWeatherInfo(weather);//直接加载数据

        }else {//没有缓存就去服务器找数据,这里只有软件第一次进入天气界面才会执行

            //根据key,取传过来的天气id
            weatherId=getIntent().getStringExtra("weather_id");

            scrollview.setVisibility(View.INVISIBLE);//数据没有加载出来前,让滚动条不可见
            requestWeather(weatherId);//请求数据
        }


        String bingPic=prefs.getString("bing_pic",null);
        if (bingPic!=null){//看看缓存中是否有背景图,有就根据key直接加载
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();//没有就去请求
        }
    }

    //根据天气id请求城市天气信息,和前面那个一样,只是这里请求的不是城市，是天气信息
    public void requestWeather(final String weatherId){
        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+
                "&key=0b675b7e86ff4294ad89692a771fe09d";

        //同前一样
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                //得到Gson类型的数组数据
                final String responseText= Objects.requireNonNull(response.body()).string();
                //利用封装的Utility工具类反序列化
                final Weather weather=Utility.handleWeatherResponse(responseText);

                runOnUiThread(() -> {
                    //在获取这个天气信息中有一个状态,如果获取天气成功，就是OK
                    if (weather!=null&&"ok".equals(weather.status)){

                        //将请求到的所有天气信息添加缓存
                        SharedPreferences.Editor editor=PreferenceManager.
                                getDefaultSharedPreferences(activity_weather.this).edit();
                        editor.putString("weather",responseText);
                        editor.apply();

                        showWeatherInfo(weather);//然后就是加载数据了
                    }else {
                        Toast.makeText(activity_weather.this,"获取天气信息异常",Toast.LENGTH_SHORT).show();
                    }
                    swipeRefresh.setRefreshing(false);//使刷新停止
                });

            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(activity_weather.this,"获取天气信息异常",Toast.LENGTH_SHORT).show();
                    swipeRefresh.setRefreshing(false);//使刷新停止
                });
            }
        });

        loadBingPic();//没有缓存要背景图片
    }


    private void showWeatherInfo(Weather weather){//加载数据

        //启动服务
        if (weather!=null&&"ok".equals(weather.status)){
            Intent intent=new Intent(this, AutoUpDateService.class);
            startService(intent);

        }else {
            Toast.makeText(activity_weather.this,"获取天气信息异常",Toast.LENGTH_SHORT).show();
        }


        assert weather != null;//判断不为空
        String cityName=weather.basic.cityName;
        String updateTime=weather.basic.update.updateTime.split(" ")[1];
        String degree=weather.now.temperature+"℃";
        String weatherInfo=weather.now.more.info;

        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);

        linearLayout.removeAllViews();//先清空一下

        for (Forecast forecast:weather.mForecastList){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,
                    linearLayout,false);//加载linearLayout适配器

            TextView dateText=view.findViewById(R.id.date_text);
            TextView infoText=view.findViewById(R.id.info_text);
            TextView maxText=view.findViewById(R.id.max_text);
            TextView minText=view.findViewById(R.id.min_text);

            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);

            linearLayout.addView(view);
            }
            if (weather.aqi!=null){
                aqiText.setText(weather.aqi.city.aqi);
                pm25Text.setText(weather.aqi.city.pm25);
            }

            String comfort="舒适度"+weather.suggestion.comfort.info;
            String carWash="洗车指数"+weather.suggestion.carWash.info;
            String sport="运动建议"+weather.suggestion.sport.info;

            comfortText.setText(comfort);
            carWashText.setText(carWash);
            sportText.setText(sport);
            scrollview.setVisibility(View.VISIBLE);

            loadBingPic();//没有缓存也要背景图片
    }

    public void loadBingPic(){//加载背景图

        String requestBingPic="http://guolin.tech/api/bing_pic";//由郭神提供的每日免费图片

        //利用封装好的工具类去服务器请求数据
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                //这里就就是一个图片地址，不需要反序列化,可以直接加载
                final String binPic= Objects.requireNonNull(response.body()).string();

                //将图片加入到缓存
                SharedPreferences.Editor editor=PreferenceManager.
                        getDefaultSharedPreferences(activity_weather.this).edit();
                editor.putString("bing_pic",binPic);
                editor.apply();//缓存图片

                runOnUiThread(() -> {//子线程中加载图片
                    //利用glide加载图片,bingPicImg要放置图片预留的位置
                    Glide.with(activity_weather.this).load(binPic).into(bingPicImg);
                });
            }
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }
        });
    }
}