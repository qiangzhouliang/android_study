package com.qzl.mymusic.serivice;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import com.qzl.mymusic.application.CodingkePlayerApp;
import com.qzl.mymusic.utils.MediaUtil;
import com.qzl.mymusic.vo.Mp3Info;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * 音乐播放的服务组件：所有的播放控制都在这里面实现
 * 使用技巧：
 * 1 用start运行，这样就不会被收回。
 * 2 什么时候用什么时候去绑定
 * 实现的功能
 * 1 、播放
 * 2 、暂停
 * 3 、上一首
 * 4 、下一首
 * 5 、获取当前的播放进度
 */
public class PlayService extends Service implements MediaPlayer.OnCompletionListener,MediaPlayer.OnErrorListener {

    private MediaPlayer mPlayer;
    private int currentPostion;//当前正在播放的位置
    public ArrayList<Mp3Info> mp3Infos;//放播放列表

    private boolean isPause = false;//是否是正在暂停状态

    private MusicUpdateListener musicUpdateListener;//声明一个属性

    /**
     * 切换播放列表
     */
    public static final int MY_MUSIC_LIST = 1;//我的音乐列表
    public static final int MY_LIKE_MUSIC_LIST = 2;//我喜欢的音乐列表
    public static final int PLAY_RECORD_MUSIC_LIST = 3;//最近播放的音乐列表
    private int ChangePlayList = MY_MUSIC_LIST;//改变播放列表
    //创建一个线程池
    private ExecutorService es = Executors.newSingleThreadExecutor();

    /**
     * 播放模式
     */
    public static final int ORDER_PLAY = 1;//顺序播放
    public static final int RANDOM_PLAY = 2;//随机
    public static final int SINGLE_PLAY = 3;//单曲循环
    private int play_mode = ORDER_PLAY;

    public int getPlay_mode() {
        return play_mode;
    }

    /**
     * @param play_mode
     * ORDER_PLAY=1
     * RANDOM_PLAY=2
     * SINGLE_PLAY=3
     */
    public void setPlay_mode(int play_mode) {

        this.play_mode = play_mode;
    }

    public boolean isPause(){
        return isPause;
    }

    public int getChangePlayList() {
        return ChangePlayList;
    }

    public void setChangePlayList(int isChangePlayList) {
        this.ChangePlayList = isChangePlayList;
    }


    public PlayService() {
    }

    public void setMp3Infos(ArrayList<Mp3Info> mp3Infos) {
        this.mp3Infos = mp3Infos;
    }

    private Random random = new Random();
    @Override
    public void onCompletion(MediaPlayer mp) {
        //播放完成后的事件监听
        switch (play_mode){
            case ORDER_PLAY:
                //顺序播放
                next();
                break;
            case RANDOM_PLAY:
                //随机
                play(random.nextInt(mp3Infos.size()));
                break;
            case SINGLE_PLAY:
                //单曲循环
                play(currentPostion);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //播放失败事件监听
        mp.reset();//重新设置
        return false;
    }

    //给activity提供对象（好处：得当当前service对象，在别的activity很方便的调运）
    public class PlayBinder extends Binder{
        public PlayService getPlayService(){
            return PlayService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        //throw new UnsupportedOperationException("Not yet implemented");
        return new PlayBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //取值（退出前保存的值）
        CodingkePlayerApp app = (CodingkePlayerApp) getApplication();
        currentPostion = app.sp.getInt("currentPostion",0);
        play_mode = app.sp.getInt("play_mode",PlayService.ORDER_PLAY);
        System.out.println("currentPostion"+currentPostion+"-----"+play_mode);

        mPlayer = new MediaPlayer();//进行实例化
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
        mp3Infos = MediaUtil.getMp3Infos(this);
        es.execute(updateStateusRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(es != null && !es.isShutdown()){
            es.shutdown();
            es = null;
        }
        mPlayer = null;
        mp3Infos = null;
        musicUpdateListener = null;
    }

    //更新进度线程
    Runnable updateStateusRunnable = new Runnable() {
        @Override
        public void run() {
            while (true){
                if(musicUpdateListener != null && mPlayer != null && mPlayer.isPlaying()){
                    musicUpdateListener.onPublish(getCurrentProgress());
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    //播放
    public void play(int position){
        Mp3Info mp3Info = null;
        if(position < 0 || position >= mp3Infos.size()) {
            position = 0;//如果是这种情况，让从第一首开始播放
        }
        mp3Info = mp3Infos.get(position);//拿到指定歌曲
        try {
            //设置播放内容
            mPlayer.reset();
            mPlayer.setDataSource(this, Uri.parse(mp3Info.getUrl()));
            mPlayer.prepare();
            mPlayer.start();
            currentPostion = position;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(musicUpdateListener != null){
            musicUpdateListener.onChange(currentPostion);
        }
    }

    //暂停
    public void pause(){
        if(mPlayer.isPlaying()){
            mPlayer.pause();
            isPause = true;
        }
    }

    //下一首
    public void next(){
        if(currentPostion+1 > mp3Infos.size()-1){
            currentPostion = 0;
        }else {
            currentPostion++;
        }
        play(currentPostion);
    }

    //上一首
    public void prev(){
        if(currentPostion - 1 < 0){
            currentPostion = mp3Infos.size()-1;
        }else {
            currentPostion--;
        }
        play(currentPostion);
    }

    //开始播放
    public void start(){
        if(mPlayer != null && !mPlayer.isPlaying()){
            mPlayer.start();

        }
    }
    //判断是否播放
    public boolean isPlaying(){
        if(mPlayer != null){
            return mPlayer.isPlaying();
        }
        return false;
    }


    public int getCurrentProgress(){
        if(mPlayer != null && mPlayer.isPlaying()){
            return mPlayer.getCurrentPosition();
        }
        return 0;
    }

    //得到当前位置
    public int getCurrentPosition(){
        return currentPostion;
    }

    public int getDuration(){
        return mPlayer.getDuration();//获取当前所在的位置
    }

    //跳到什么地方
    public void seekTo(int msec){
        mPlayer.seekTo(msec);
    }

    //更新状态的接口（观察者事件）
    public interface MusicUpdateListener{
        public void onPublish(int progress);//进度条的更新
        public void onChange(int position);//更新当前所在的位置

    }

    public void setMusicUpdateListener(MusicUpdateListener musicUpdateListener) {
        this.musicUpdateListener = musicUpdateListener;
    }

    public ArrayList<Mp3Info> getMp3Infos() {
        return mp3Infos;
    }
}
