package com.example.weather.db;

import org.litepal.crud.DataSupport;

//省信息表
public class Province extends DataSupport {
    private int id;//省代号
    private String provinceName;//省名
    private int provinceCode;//省编号

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }
}
