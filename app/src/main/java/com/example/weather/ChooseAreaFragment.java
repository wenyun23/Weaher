package com.example.weather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.weather.databinding.FragmentChooseAreaBinding;
import com.example.weather.db.City;
import com.example.weather.db.County;
import com.example.weather.db.Province;
import com.example.weather.util.HttpUtil;
import com.example.weather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment{
    private FragmentChooseAreaBinding binding;//利用viewBinding进行单向数据绑定

    //定义三个状态·表示省，市，县
    public static final int  LEVEL_PROVINCE=0;
    public static final int  LEVEL_CITY=1;
    public static final int  LEVEL_COUNTY=2;

    //利用ProgressDialog弹窗提升进度,这是系统自带的
    private ProgressDialog progressDialog;

    private ArrayAdapter<String> myAdapter;

    //定义四个个集合分别用来表示:
    // 1.当前点击的信息(所有省、所有市或者所有县);2.存省;3.存市;4.存县
    private final List<String> dataList=new ArrayList<>();
    private List<Province> mProvinceList;
    private List<City> mCityList;
    private List<County> mCountyList;

    //查询省、市
    private Province selectedProvince;
    private City selectedCity;

    //当前点击的区域,0表示点击的是"省"，1表示点击"市"，2表示点击"县"
    private int currentLevel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding=FragmentChooseAreaBinding.inflate(getLayoutInflater());

        //使用内置ArrayAdapter适配器,数据为dataList
        myAdapter=new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1,dataList);
        //给listView添加适配器
        binding.listView.setAdapter(myAdapter);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        QueryProvince();//进入界面默认加载省数据

        //给listView添加点击事件
        binding.listView.setOnItemClickListener((adapterView, view, i, l) -> {
            if (currentLevel==LEVEL_PROVINCE){//如果状态为0,表示"省"
                selectedProvince=mProvinceList.get(i);//获取当前点击的是那个省
                queryCity();//查询市

            }else if (currentLevel==LEVEL_CITY){//如果状态为1,表示"市"
                selectedCity=mCityList.get(i);
                queryCounty();

            }else if (currentLevel==LEVEL_COUNTY){//如果状态为2,表示"县"
                //多获取一个天气id
                String weatherId=mCountyList.get(i).getWeatherId();

                if (getActivity() instanceof  MainActivity){//如果不是天气界面,也就是第一次打开这个APP
                    Intent intent=new Intent(getActivity(),activity_weather.class);
                    //并将天气id传过去
                    intent.putExtra("weather_id",weatherId);
                    startActivity(intent);//跳转到天气界面
                    getActivity().finish();

                }else if (getActivity() instanceof activity_weather){//如果已经是天气界面
                    activity_weather activity_weather= (activity_weather) getActivity();

                    activity_weather.drawerLayout.closeDrawers();//关闭左侧抽屉式布局
                    activity_weather.swipeRefresh.setRefreshing(true);//刷新开始

                    activity_weather.weatherId=weatherId;
                    activity_weather.requestWeather(weatherId);
                }
            }
        });

       binding.backButton.setOnClickListener(view -> {
           if (currentLevel==LEVEL_COUNTY){//如果是在县页面返回,则获取市信息
               queryCity();//重新调用一次查询就可以
           }else if(currentLevel==LEVEL_CITY){//如果是市级返回，则获取省信息
               QueryProvince();
           }
       });

    }

    private void QueryProvince(){//查询所有省
        binding.titleText.setText("中国");
        binding.backButton.setVisibility(View.INVISIBLE);//"省"界面,按钮不显示
        //litepal可以直接使用DataSupport.findAll从对应表中查询
        mProvinceList= DataSupport.findAll(Province.class);

        if (mProvinceList.size()>0){//判断表中是否有内容
            dataList.clear();//清空dataList集合
            for (Province province:mProvinceList){//循环将省名添加进集合
                dataList.add(province.getProvinceName());
            }

            binding.listView.setSelection(0);//回到顶部
            myAdapter.notifyDataSetChanged();//刷新界面
            currentLevel=LEVEL_PROVINCE;//状态为"0",即"省"
        }else {//表中没有东西就取服务器请求
            //("服务器网址","加载的类型")
            queryFormService("http://guolin.tech/api/china","province");
        }
    }

    private void queryCity(){//查询市,同上

        binding.titleText.setText(selectedProvince.getProvinceName());//市的界面设置省名字
        binding.backButton.setVisibility(View.VISIBLE);

        //这里和查询省,是一样的,只是多一个省id条件
        mCityList=DataSupport.where("provinceId =?",
                String.valueOf(selectedProvince.getId())).find(City.class);

        if (mCityList.size()>0){//同上
            dataList.clear();
            for (City city:mCityList){
                dataList.add(city.getCityName());
            }
            myAdapter.notifyDataSetChanged();
            binding.listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else {
            int provinceCode=selectedProvince.getProvinceCode();
            queryFormService("http://guolin.tech/api/china/"+provinceCode,"city");
        }
    }

    private void queryCounty(){//查询县,同上

        binding.titleText.setText(selectedCity.getCityName());
        binding.backButton.setVisibility(View.VISIBLE);

        mCountyList=DataSupport.where("cityid=?",
                String.valueOf(selectedCity.getId())).find(County.class);

        if (mCountyList.size()>0){
            dataList.clear();
            for (County county:mCountyList){
                dataList.add(county.getCountyName());
            }
            myAdapter.notifyDataSetChanged();
            binding.listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else {
            int provinceCode=selectedProvince.getProvinceCode();
            int cityCode=selectedCity.getCityCode();
            queryFormService("http://guolin.tech/api/china/"+provinceCode+"/"+cityCode,
                    "county");

        }
    }


    //queryFormService方法用于:根据传入的地址和服务器类型从服务器上查询"省、市、县"数据
    private void queryFormService(String address,final String type){

        showProgressDialog();//解析前显示正在加载

        //调用工具类的方法向服务器请求数据
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            //重写回调的两个方法

            @Override//加载成功
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                //回调后得到的是gson类型的数组数据例如:[{"id":1,"name":"北京"},{"id":2,"name":"上海"}]
                String responseText = Objects.requireNonNull(response.body()).string();
                boolean result = false;
                if ("province".equals(type)) {//如果类型是"省"
                    //利用封装好的工具类(Utility),反序列数据Gson数据
                    result = Utility.handleProvinceResponse(responseText);

                } else if ("city".equals(type)) {//如果类型是"市"
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());

                } else if ("county".equals(type)) {//如果类型是"县"
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                if (result){//如果是ture表示反序列化成功,false,反之

                    //利用子线程实现从表中取出内容放到dataList中
                    Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
                        closeProgressDialog();//关闭"正在加载"对话框
                        if ("province".equals(type)){
                            QueryProvince();
                        }else if ("city".equals(type)){
                            queryCity();
                        }else {
                            queryCounty();
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {//加载失败
                Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
                    closeProgressDialog();//关闭显示正在加载
                    Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showProgressDialog(){
        if (progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private  void closeProgressDialog(){
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}