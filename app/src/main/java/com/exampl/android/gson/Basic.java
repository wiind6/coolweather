package com.exampl.android.gson;

import com.google.gson.annotations.SerializedName;


/**
 * Created by dupen on 2018/1/15.
 */

public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;
    public Update update;
    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
