//用来实现选择城市的操作
package com.example.ghm.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.ghm.app.MyApplication;
import com.example.ghm.bean.City;

import java.util.ArrayList;
import java.util.List;

public class SelectCity extends Activity implements View.OnClickListener{

    private ImageView mBackBtn;
    //private String[] data = {"apple","banana","orange"};



    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);

        List<City> mCityList = MyApplication.getCityList();
        final String[] data = new String[mCityList.size()];
        final String[] data_num = new String[mCityList.size()];
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

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                SelectCity.this, android.R.layout.simple_list_item_1, data);
        ListView listView = (ListView)findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent();
                i.putExtra("cityCode",data_num[position]);
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
                i.putExtra("cityCode","101160101");
                setResult(RESULT_OK, i);
                finish();
                break;
                default:
                    break;

        }
    }
}
