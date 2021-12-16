package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //这里就是将第一次运行这个软件，MainActivity这个界面的城市信息添加缓存,加不加都意义不大
        SharedPreferences pers= PreferenceManager.getDefaultSharedPreferences(this);
        if (pers.getString("weather",null)!=null){
            Intent intent=new Intent(this,activity_weather.class);
            startActivity(intent);
            finish();
        }
    }
}