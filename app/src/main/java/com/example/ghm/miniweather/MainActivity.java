package com.example.ghm.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.ghm.app.MyApplication;
import com.example.ghm.bean.City;
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
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements View.OnClickListener {

    private static final int UPDATE_TODAY_WEATHER = 1;

    private ImageView mUpdateBtn;

    //初始化界面的控件
    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv, temperatureTv, climateTv, windTv, city_name_Tv;
    private ImageView weatherImg, pmImg;

    private ImageView mCitySelect,mTitleLocation;

    private ProgressBar mUpdateProgressBar;

    private String mCurCityCode;
    private SharedPreferenceUtil mSpUtil;
    private MyApplication myApplication;

    private java.util.List<City> mCityList;
    private String mLocCityCode;
    private String cityName;
    private  ImageView mtitleLocation;

    public LocationClient mLocationClient = null;
    private MyLocationListener myLocationListerner = new MyLocationListener();

    private PagerAdapter vpAdapter;
    private ViewPager vp;
    private List<View> views;
    private int[] ids = {R.id.iv1, R.id.iv2};
    private TextView week_today,temperature,climate,wind,week_today1,temperature1,climate1,wind1,
            week_today2,temperature2,climate2,wind2, week_today3,temperature3,climate3,wind3,week_today4,temperature4,climate4,wind4,
            week_today5,temperature5,climate5,wind5;






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

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myLocationListerner);
        initLocation();

        //调用初始化控件函数
        initView();
//        initDots();
//        initViews();
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
        if(view.getId() == R.id.title_location){

            Toast.makeText(MainActivity.this, "aaa", Toast.LENGTH_LONG).show();


            if(mLocationClient.isStarted()){
                mLocationClient.stop();
            }
            mLocationClient.start();
            final int DB = 1000;

            final Handler BDHamdler = new Handler(){
                public void handleMessage(Message msg){
                    switch (msg.what){
                        case DB:
                            if(msg.obj != null){
                                if(NetUtil.getNetworkState(MainActivity.this) != NetUtil.NETWORN_NONE){
                                    queryWeatherCode(myLocationListerner.cityCode);
                                }
                                else {
                                    Toast.makeText(MainActivity.this,"网络异常",Toast.LENGTH_LONG).show();
                                }
                            }
                            myLocationListerner.cityCode = null;
                            break;
                        default:
                            break;
                    }
                }
            };
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        while(myLocationListerner.cityCode == null){
                            Thread.sleep(2000);
                        }
                        Message msg = new Message();
                        msg.what = DB;
                        msg.obj = myLocationListerner.cityCode;
                        BDHamdler.sendMessage(msg);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
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
//
//        week_today = views.get(0).findViewById(R.id.week_today);
//        temperature = views.get(0).findViewById(R.id.temperature);
//        climate = views.get(0).findViewById(R.id.climate);
//        wind = views.get(0).findViewById(R.id.wind);
//
//        week_today1 = views.get(0).findViewById(R.id.week_today1);
//        temperature1 = views.get(0).findViewById(R.id.temperature1);
//        climate1 = views.get(0).findViewById(R.id.climate1);
//        wind1 = views.get(0).findViewById(R.id.wind1);
//
//        week_today2 = views.get(0).findViewById(R.id.week_today2);
//        temperature2 = views.get(0).findViewById(R.id.temperature2);
//        climate2 = views.get(0).findViewById(R.id.climate2);
//        wind2 = views.get(0).findViewById(R.id.wind2);
//
//        week_today3 = views.get(0).findViewById(R.id.week_today3);
//        temperature3 = views.get(0).findViewById(R.id.temperature3);
//        climate1 = views.get(0).findViewById(R.id.climate3);
//        wind1 = views.get(0).findViewById(R.id.wind3);
//
//        week_today4 = views.get(0).findViewById(R.id.week_today4);
//        temperature4 = views.get(0).findViewById(R.id.temperature4);
//        climate4 = views.get(0).findViewById(R.id.climate4);
//        wind4 = views.get(0).findViewById(R.id.wind4);
//
//        week_today5 = views.get(0).findViewById(R.id.week_today5);
//        temperature5 = views.get(0).findViewById(R.id.temperature5);
//        climate5 = views.get(0).findViewById(R.id.climate5);
//        wind5 = views.get(0).findViewById(R.id.wind5);



//        week_today.setText("N/A");
//        temperature.setText("N/A");
//        climate.setText("N/A");
//        wind.setText("N/A");
//
//        week_today1.setText("N/A");
//        temperature1.setText("N/A");
//        climate1.setText("N/A");
//        wind1.setText("N/A");
//
//        week_today2.setText("N/A");
//        temperature2.setText("N/A");
//        climate2.setText("N/A");
//        wind2.setText("N/A");
//
//        week_today3.setText("N/A");
//        temperature3.setText("N/A");
//        climate1.setText("N/A");
//        wind1.setText("N/A");
//
//        week_today4.setText("N/A");
//        temperature4.setText("N/A");
//        climate4.setText("N/A");
//        wind4.setText("N/A");
//
//        week_today5.setText("N/A");
//        temperature5.setText("N/A");
//        climate5.setText("N/A");
//        wind5.setText("N/A");

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

    //配置定位的SDK函数
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        int span = 1000;
        option.setScanSpan(0);

        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setLocationNotify(true);
        option.setIsNeedLocationPoiList(true);
        option.setIsNeedLocationDescribe(true);
        option.SetIgnoreCacheException(false);
        option.setIgnoreKillProcess(false);
        option.setEnableSimulateGps(false);

    }

    void  initDots(){
        ImageView[] dots = new ImageView[views.size()];
        for (int i = 0; i<views.size();i++){
            dots[i] = (ImageView)findViewById(ids[i]);
        }
    }

    private void initViews() {
        LayoutInflater inflater = LayoutInflater.from(this);
        views = new ArrayList<View>();
        views.add(inflater.inflate(R.layout.sixday1, null));
        views.add(inflater.inflate(R.layout.sixday2, null));
        vpAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return 0;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
                return false;
            }
        };
        vp = (ViewPager) findViewById(R.id.viewpager);
        vp.setAdapter(vpAdapter);
        //vp.setOnPageChangeListener();
    }

        public void onPageScrolled(int position, float positionOffset,int positionOffsetPixels){

        }

        public void onPageSelected(int position) {
            ImageView[] dots = new ImageView[views.size()];
            for (int a = 0; a < ids.length; a++) {


            }
        }
        public void onPageScrollStateChanged(int state){

        }

}



