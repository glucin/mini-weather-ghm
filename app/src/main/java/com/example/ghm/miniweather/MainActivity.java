package com.example.ghm.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ghm.app.MyApplication;
import com.example.ghm.bean.TodayWeather;
import com.example.ghm.util.NetUtil;
import com.example.ghm.util.SharedPreferenceUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final int UPDATE_TODAY_WEATHER = 1;

    private ImageView mUpdateBtn;

    //初始化界面的控件
    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv, temperatureTv, climateTv, windTv, city_name_Tv;
    private ImageView weatherImg, pmImg;

    private ImageView mCitySelect;

    private ProgressBar mUpdateProgressBar;

    private String mCurCityCode;
    private SharedPreferenceUtil mSpUtil;
    private MyApplication myApplication;

    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg){
           switch (msg.what){
               case UPDATE_TODAY_WEATHER:
                   updateTodayWeather((TodayWeather)msg.obj);
                   mUpdateBtn.setVisibility(View.VISIBLE);
                   mUpdateProgressBar.setVisibility((View.GONE));
                   break;
               default:
                   break;
           }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        myApplication = MyApplication.getInstance();
        mSpUtil = myApplication.getSharePreferenceUtil();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);

        mUpdateBtn = (ImageView) findViewById((R.id.title_update_btn));
        mUpdateBtn.setOnClickListener(this);

        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
            Log.d("myWeather", "网络正常");
            Toast.makeText(MainActivity.this, "网络正常", Toast.LENGTH_LONG).show();
        } else {
            Log.d("myWeather", "网络异常");
            Toast.makeText(MainActivity.this, "网络异常", Toast.LENGTH_LONG).show();
        }

        mCitySelect = (ImageView) findViewById(R.id.title_city_manger);
        mCitySelect.setOnClickListener(this);

        mUpdateProgressBar = (ProgressBar)findViewById(R.id.title_update_progress);

        //调用初始化控件函数
        initView();
    }

    private void queryWeatherCode(String cityCode) {
        mUpdateBtn.setVisibility(View.GONE);
        mUpdateProgressBar.setVisibility(View.VISIBLE);
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeather", address);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;
                TodayWeather todayWeather = null;
                try {
                    URL url = new URL(address);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while ((str = reader.readLine()) != null) {
                        response.append(str);
                        Log.d("myWeather", str);
                    }
                    String responseStr = response.toString();
                    Log.d("myWeather", responseStr);
                    todayWeather = parseXML(responseStr);

                    if(todayWeather != null){
                        Log.d("myWeather",todayWeather.toString());

                        Message msg = new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj = todayWeather;
                        mHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                }
            }
        }).start();
    }

    //单击事件




    public void onClick(View view) {
        if(view.getId() == R.id.title_city_manger){
            Intent i = new Intent(this, SelectCity.class);
            //startActivity(i);
            startActivityForResult(i,1);
        }
        if (view.getId() == R.id.title_update_btn) {
            //通过SharedPreferences读取城市ID
           // SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
           // String cityCode = sharedPreferences.getString("main_city_code", "101010100");
          //  Log.d("myWeather", cityCode);

            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather", "网络正常");
                String cityCode;
                if(TextUtils.isEmpty((mSpUtil.getCurrCityCode()))){
                    mSpUtil.setCurrCityCode("101010100");
                    cityCode = mSpUtil.getCurrCityCode();

                }
                else{
                    cityCode = mSpUtil.getCurrCityCode();
                    queryWeatherCode(cityCode);

                }

            } else {
                Log.d("myWeather", "网络异常");
                Toast.makeText(MainActivity.this, "网络异常", Toast.LENGTH_LONG).show();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == 1 && resultCode == RESULT_OK){
            //接收SelectCity发送的消息
            String newCityCode = data.getStringExtra("cityCode");
            Log.d("myWeather","选择的城市代码为"+newCityCode);

            if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
                Log.d("myWeather"," 网络正常");
                mSpUtil.setCurrCityCode(newCityCode);
                queryWeatherCode((newCityCode));
            }
            else {
                Log.d("myWeather","网络异常");
                Toast.makeText(MainActivity.this ,"网络异常",Toast.LENGTH_LONG).show();

            }
        }
    }

    //获取并解析网络数据
    //返回todayWeather类型的数据
    private TodayWeather parseXML(String xmldata) {
        TodayWeather todayWeather = null;
        int fengxiangCount = 0;
        int fengliCount = 0;
        int dateCount = 0;
        int highCount = 0;
        int lowCount = 0;
        int typeCount = 0;
        try {
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather", "parseXML");
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp")){
                            todayWeather = new TodayWeather();
                        }
                        if(todayWeather != null){
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                                //Log.d("myWeather", "city:    " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                                //Log.d("myWeather", "updatetime:  " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                                //Log.d("myWeather", "shidu:  " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                                //Log.d("myWeather", "wendu:  " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                                //Log.d("myWeather", "pm25:  " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                                //Log.d("myWeather", "quality:  " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                //Log.d("myWeather", "fengxiang:  " + xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                //Log.d("myWeather", "fengli:  " + xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                //Log.d("myWeather", "date:  " + xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText());
                                //Log.d("myWeather", "high:  " + xmlPullParser.getText());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText());
                                //Log.d("myWeather", "low:  " + xmlPullParser.getText());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                //Log.d("myWeather", "type:  " + xmlPullParser.getText());
                                typeCount++;
                            }
                        }

                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return todayWeather;
    }

    //初始化控件内容
    void initView() {
        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality);
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView) findViewById(R.id.temperature);
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);
        weatherImg = (ImageView) findViewById(R.id.weather_img);
        city_name_Tv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");
    }
    void updateTodayWeather(TodayWeather todayWeather) {
        city_name_Tv.setText(todayWeather.getCity() + "天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime() + "发布");
        humidityTv.setText("湿度:" + todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh() + "~" + todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力:" + todayWeather.getFengli());
        if(todayWeather.getType().equals("多云")){
            weatherImg.setImageDrawable(getResources().getDrawable((R.drawable.biz_plugin_weather_duoyun)));
        }
        else if(todayWeather.getType().equals("暴雪")){
            weatherImg.setImageDrawable(getResources().getDrawable((R.drawable.biz_plugin_weather_baoxue)));
        }
        else if(todayWeather.getType().equals("暴雨")){
            weatherImg.setImageDrawable(getResources().getDrawable((R.drawable.biz_plugin_weather_baoyu)));
        }
        else if(todayWeather.getType().equals("大暴雨")){
            weatherImg.setImageDrawable(getResources().getDrawable((R.drawable.biz_plugin_weather_dabaoyu)));
        }
        else if(todayWeather.getType().equals("大雪")){
            weatherImg.setImageDrawable(getResources().getDrawable((R.drawable.biz_plugin_weather_daxue)));
        }
        else if(todayWeather.getType().equals("大雨")){
            weatherImg.setImageDrawable(getResources().getDrawable((R.drawable.biz_plugin_weather_dayu)));
        }
        else if(todayWeather.getType().equals("雷阵雨")){
            weatherImg.setImageDrawable(getResources().getDrawable((R.drawable.biz_plugin_weather_leizhenyu)));
        }
        else if(todayWeather.getType().equals("雷阵雨冰雹")){
            weatherImg.setImageDrawable(getResources().getDrawable((R.drawable.biz_plugin_weather_leizhenyubingbao)));
        }
        else if(todayWeather.getType().equals("沙尘暴")){
            weatherImg.setImageDrawable(getResources().getDrawable((R.drawable.biz_plugin_weather_shachenbao)));
        }
        else if(todayWeather.getType().equals("特大暴雨")){
            weatherImg.setImageDrawable(getResources().getDrawable((R.drawable.biz_plugin_weather_tedabaoyu)));
        }
        else if(todayWeather.getType().equals("雾")){
            weatherImg.setImageDrawable(getResources().getDrawable((R.drawable.biz_plugin_weather_wu)));
        }
        else if(todayWeather.getType().equals("小雪")){
            weatherImg.setImageDrawable(getResources().getDrawable((R.drawable.biz_plugin_weather_xiaoxue)));
        }
        else if(todayWeather.getType().equals("小雨")){
            weatherImg.setImageDrawable(getResources().getDrawable((R.drawable.biz_plugin_weather_xiaoyu)));
        }
        else if(todayWeather.getType().equals("阴")){
            weatherImg.setImageDrawable(getResources().getDrawable((R.drawable.biz_plugin_weather_yin)));
        }
        else if(todayWeather.getType().equals("雨夹雪")){
            weatherImg.setImageDrawable(getResources().getDrawable((R.drawable.biz_plugin_weather_yujiaxue)));
        }
        else if(todayWeather.getType().equals("阵雨")){
            weatherImg.setImageDrawable(getResources().getDrawable((R.drawable.biz_plugin_weather_zhenyu)));
        }
        else if(todayWeather.getType().equals("阵雪")){
            weatherImg.setImageDrawable(getResources().getDrawable((R.drawable.biz_plugin_weather_zhenxue)));
        }
        else if(todayWeather.getType().equals("中雪")){
            weatherImg.setImageDrawable(getResources().getDrawable((R.drawable.biz_plugin_weather_zhongxue)));
        }
        else if(todayWeather.getType().equals("中雨")){
            weatherImg.setImageDrawable(getResources().getDrawable((R.drawable.biz_plugin_weather_zhongyu)));
        }
        if(todayWeather.getPm25() != null) {
            int pm = Integer.parseInt(todayWeather.getPm25());
            if (pm <= 50) {
                pmImg.setImageDrawable(getResources().getDrawable((R.drawable.biz_plugin_weather_0_50)));
            } else if (pm > 50 && pm <= 100) {
                pmImg.setImageDrawable(getResources().getDrawable((R.drawable.biz_plugin_weather_51_100)));
            } else if (pm > 100 && pm <= 150) {
                pmImg.setImageDrawable(getResources().getDrawable((R.drawable.biz_plugin_weather_101_150)));
            } else if (pm > 150 && pm <= 200) {
                pmImg.setImageDrawable(getResources().getDrawable((R.drawable.biz_plugin_weather_151_200)));
            } else if (pm > 200 && pm <= 300) {
                pmImg.setImageDrawable(getResources().getDrawable((R.drawable.biz_plugin_weather_201_300)));
            } else if (pm > 300) {
                pmImg.setImageDrawable(getResources().getDrawable((R.drawable.biz_plugin_weather_greater_300)));
            }
        }




        Toast.makeText(MainActivity.this, "更新成功!", Toast.LENGTH_SHORT).show();
    }
}


