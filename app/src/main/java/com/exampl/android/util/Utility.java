package com.exampl.android.util;

import android.text.TextUtils;

import com.exampl.android.db.City;
import com.exampl.android.db.County;
import com.exampl.android.db.Province;
import com.exampl.android.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dupen on 2018/1/14.
 */

public class Utility {
    public static boolean handleProvinceReponse(String responce){
        if(!TextUtils.isEmpty(responce)){
            try {
                JSONArray Allprovinces = new JSONArray(responce);
                for (int i =0;i<Allprovinces.length();i++){
                    JSONObject provinceObject = Allprovinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    public static boolean handleCityReponse(String responce,int provinceId){
        if(!TextUtils.isEmpty(responce)){
            try {
                JSONArray allCities = new JSONArray(responce);
                for (int i =0;i<allCities.length();i++){
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCountyReponse(String responce,int cityId){
        if(!TextUtils.isEmpty(responce)){
            try {
                JSONArray allCounties = new JSONArray(responce);
                for (int i =0;i<allCounties.length();i++){
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static Weather handleWeatherResponse(String responce){
        try {
            JSONObject jsonObject = new JSONObject(responce);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
