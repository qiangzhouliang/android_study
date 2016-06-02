package com.qzl.mymusic;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;

import com.qzl.mymusic.serivice.PlayService;
import com.qzl.mymusic.utils.Constant;

import java.util.ArrayList;

/**
 * 做绑定工作
 * Created by Q on 2016-05-09.
 */
public abstract class BaseActivity extends FragmentActivity {

    public PlayService playService;
    private ArrayList<Activity> list = new ArrayList<>();
    private boolean isBound = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        list.add(this);
    }

    //退出功能
    public void exit(){
        for (int i = 0;i<list.size();i++){
            list.get(i).finish();
        }
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayService.PlayBinder playBinder = (PlayService.PlayBinder)service;
            //拿到service对象
            playService = playBinder.getPlayService();
            playService.setMusicUpdateListener(musicUpdateListener);
            musicUpdateListener.onChange(playService.getCurrentPosition());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            playService = null;
            isBound = false;
        }
    };

    private PlayService.MusicUpdateListener musicUpdateListener = new PlayService.MusicUpdateListener() {
        @Override
        public void onPublish(int progress) {
            publish(progress);
        }

        @Override
        public void onChange(int position) {
            change(position);
        }
    };

    public abstract void publish(int progress);
    public abstract void change(int position);

    //绑定服务：由子类来决定什么时候去调运
    public void bindPlayService(){
        if(!isBound){
            Intent intent = new Intent(this,PlayService.class);
            bindService(intent,conn, Context.BIND_AUTO_CREATE);
            isBound = true;
        }

    }

    //解除绑定服务
    public void unbindPlayService(){
        if(isBound){
            unbindService(conn);
            isBound = false;
        }

    }
}
