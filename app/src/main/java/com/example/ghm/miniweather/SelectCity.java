//用来实现选择城市的操作
package com.example.ghm.miniweather;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ghm.app.MyApplication;
import com.example.ghm.bean.City;
import com.example.ghm.bean.Pinyin;
import com.example.ghm.util.SharedPreferenceUtil;
//import com.example.ghm.miniweather.ClearEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectCity extends Activity implements View.OnClickListener{

    private ImageView mBackBtn;
//    private SearchView searchView;
    private ArrayList<String> mSearchResult = new ArrayList<>();
    private Map<String,String> nameToCode = new HashMap<>();
    private Map<String,String> nameToPinyin = new HashMap<>();
    private SharedPreferenceUtil mSpUtil;
    private MyApplication myApplication;
    private HashMap<String, City> cityCode_cityHashMap;
    //private String[] data = {"apple","banana","orange"};

    //   mClearEditText = (ClearEditText)findViewById(R.id.search_city);


    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);


        List<City> mCityList = MyApplication.getCityList();
        final String[] data = new String[mCityList.size()];
        final String[] data_num = new String[mCityList.size()];
        myApplication = MyApplication.getInstance();
        mSpUtil = myApplication.getSharePreferenceUtil();
        cityCode_cityHashMap = new HashMap<>();
        for(City city : mCityList){
            cityCode_cityHashMap.put(city.getNumber(),city);
        }
        City curCity = cityCode_cityHashMap.get(mSpUtil.getCurrCityCode());
        int i = 0;
        for(City city : mCityList){
            data[i] = city.getCity();
            data_num[i] = city.getNumber();
            if(i == 0){
                i++;
                continue;
            }
            if(data_num[i].substring(0,5).equals(data_num[i-1].substring(0,5))){
                data[i] = "      ".concat(data[i]);
                i++;
            }
            else i++;
           // data[i] = data[i++].concat(city.getNumber());
        }

        mBackBtn = (ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);
        final ArrayAdapter<String> adapter;
        for(City city : mCityList){
            String strCode = city.getNumber();
            String strName = city.getCity();
            String strNamePinyin = Pinyin.converterToSpell(strName);
            nameToCode.put(strName,strCode);
            nameToPinyin.put(strName,strNamePinyin);
            mSearchResult.add(strName);

        }
        if(mSearchResult == null) {
            adapter = new ArrayAdapter<String>(
                    SelectCity.this, android.R.layout.simple_list_item_1, data);

        }
        else{
            adapter = new ArrayAdapter<String>(
                    SelectCity.this, android.R.layout.simple_list_item_1, mSearchResult);
        }
        final android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView)findViewById(R.id.search_city);
        searchView.setIconified(true);
        searchView.setQueryHint("搜索城市");
        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)) {
                    if (mSearchResult != null)
                        mSearchResult.clear();
                    for (String str : nameToPinyin.keySet()) {
                        if (str.contains(newText) || nameToPinyin.get(str).contains(newText)) {
                            mSearchResult.add(str);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
        });
        final ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);
//
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                final List<City> listcity = MyApplication.getInstance().getCityList();
//                String select_cityCode = listcity.get(position).getNumber();
//                Intent i = new Intent();
//                i.putExtra("cityCode", select_cityCode);
////                Intent i = new Intent();
////
////                i.putExtra("cityCode", data_num[position]);
//                setResult(RESULT_OK, i);
//                finish();
//            }
//        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Toast.makeText(SelectCity.this ,nameToCode.get(mSearchResult.get(position)),Toast.LENGTH_LONG).show();
                String select_cityCode = nameToCode.get(mSearchResult.get(position));
                Intent i = new Intent();
                i.putExtra("cityCode", select_cityCode);
                setResult(RESULT_OK, i);
                finish();
            }
        });
        }

    //选择城市图标响应
    public void onClick(View v){
        switch (v.getId()){
            case R.id.title_back:
                //发送数据给MainActivity
                Intent i = new Intent();
                i.putExtra("cityCode","101010100");
                setResult(RESULT_OK, i);
                finish();
                break;
                default:
                    break;

        }
    }
}
