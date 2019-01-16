package com.example.ghm.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceUtil {
    public SharedPreferences sp;
    public SharedPreferences.Editor spEditor;
    public static final String CITY_SHAREPRE_FILE = "city";
    public  static final String CURR_CITY_CODE = "curCityCode";
    public SharedPreferenceUtil(Context context, String file){
        sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        spEditor = sp.edit();
    }
    public void setCurrCityCode(String currCityCode){
        spEditor.putString(CURR_CITY_CODE, currCityCode);
        spEditor.commit();
    }
    public String getCurrCityCode(){
        return sp.getString(CURR_CITY_CODE, "");
    }
}
