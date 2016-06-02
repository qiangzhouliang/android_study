package com.qzl.mymusic.application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.lidroid.xutils.DbUtils;
import com.qzl.mymusic.utils.Constant;

/**
 * Created by Q on 2016-05-13.
 */
public class CodingkePlayerApp extends Application {

    public static SharedPreferences sp;
    public static DbUtils dbUtils;
    public static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        sp = getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        dbUtils = DbUtils.create(getApplicationContext(),Constant.DB_NAME);
        context = getApplicationContext();
    }
}
