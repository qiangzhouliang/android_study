package com.qzl.mymusic;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.WindowManager;

import com.qzl.mymusic.serivice.PlayService;

/**
 * 启动页面，并启动service
 */
public class SplashActivity extends Activity {


    private static final int START_ACTIVITY = 0x1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去标题，全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_fullscreen);

        Intent intent = new Intent(this,PlayService.class);
        //启动服务
        startService(intent);

        handler.sendEmptyMessageDelayed(START_ACTIVITY,3000);

    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case START_ACTIVITY:
                    //启动主界面，将自己finish掉
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                    break;
            }
        }
    };
}
