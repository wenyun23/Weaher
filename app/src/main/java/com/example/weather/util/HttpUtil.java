package com.example.weather.util;

import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;
//封装一个工具类,利用OkHttpClient请求服务器的数据
public class HttpUtil {

    //Callback是okhttp3处理回调
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback){

        //创建一个OkHttpClient实例
        OkHttpClient client=new OkHttpClient();

        //创建一个request发起请求
        Request request=new Request.Builder().url(address).build();
        //解析得到Request{method=GET, url=http://guolin.tech/api/china}
        Log.d("TAG", "sendOkHttpRequest: "+request);

        //之后回调OkHttpClient的newVall()方法来创建一个cail对象,并调用它的execute方法
        //来发起请求，并获取服务器返回的数据
        client.newCall(request).enqueue(callback);
    }
}
