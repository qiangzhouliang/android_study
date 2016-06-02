package com.qzl.mymusic;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.qzl.mymusic.adapter.MyMusicListAdapter;
import com.qzl.mymusic.application.CodingkePlayerApp;
import com.qzl.mymusic.vo.Mp3Info;

import java.util.ArrayList;
import java.util.List;

public class MyLikeMusicListActivity extends BaseActivity implements AdapterView.OnItemClickListener{

    private ListView listView_like;
    private CodingkePlayerApp app;
    private ArrayList<Mp3Info> likemp3Infos;
    private MyMusicListAdapter adapter;
    private boolean isChange = false;//表示当前播放列表是否为收藏列表

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_like_music_list);

        app = (CodingkePlayerApp) getApplication();
        listView_like = (ListView) findViewById(R.id.listView_like);

        listView_like.setOnItemClickListener(this);
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindPlayService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindPlayService();
    }

    private void initData() {
        try {
            List<Mp3Info> list = app.dbUtils.findAll(Selector.from(Mp3Info.class).where("isLike","=","1"));
            if(list == null || list.size() == 0){
                return;
            }
            likemp3Infos = (ArrayList<Mp3Info>) list;
            adapter = new MyMusicListAdapter(this,likemp3Infos);
            listView_like.setAdapter(adapter);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void publish(int progress) {

    }

    @Override
    public void change(int position) {

    }

    //列表单击事件
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(playService.getChangePlayList() != playService.MY_LIKE_MUSIC_LIST){
            playService.setMp3Infos(likemp3Infos);//切换播放列表
            playService.setChangePlayList(playService.MY_LIKE_MUSIC_LIST);
        }
        playService.play(position);
        //保存播放时间
        savePlayRecord();
    }
    //保存播放记录
    private void savePlayRecord() {
        //获取当前正在播放的音乐播放
        Mp3Info mp3Info = playService.getMp3Infos().get(playService.getCurrentPosition());
        try{
            Mp3Info playRecordMp3Info = app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId", "=",mp3Info.getMp3InfoId()));
            if(playRecordMp3Info==null){
                mp3Info.setPlayTime(System.currentTimeMillis());
                app.dbUtils.save(mp3Info);
            }else{
                playRecordMp3Info.setPlayTime(System.currentTimeMillis());
                app.dbUtils.update(playRecordMp3Info, "playTime");
            }
        }catch(DbException e){
            e.printStackTrace();
        }
    }
}
