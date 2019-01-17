package com.example.ghm.miniweather;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.example.ghm.app.MyApplication;
import com.example.ghm.bean.City;

import java.util.List;

public class MyLocationListener extends BDAbstractLocationListener{
    public String recity;
    public String cityCode;
    public void onReceiveLocation(BDLocation location){
        String addr = location.getAddrStr();
        String country = location.getCountry();
        String province = location.getProvince();
        String city = location.getCity();
        String district = location.getCity();
        String street = location.getStreet();
        recity = city.replace("市","");

        List<City> mCityList;
        MyApplication myApplication;
        myApplication = MyApplication.getInstance();

        mCityList = myApplication.getCityList();

        for(City cityl:mCityList){
            if(cityl.getCity().equals(recity)){
                cityCode = cityl.getNumber();
            }
        }
    }
}
