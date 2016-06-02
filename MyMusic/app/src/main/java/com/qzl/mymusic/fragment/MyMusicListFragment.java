package com.qzl.mymusic.fragment;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.andraskindler.quickscroll.QuickScroll;
import com.andraskindler.quickscroll.Scrollable;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.qzl.mymusic.MainActivity;
import com.qzl.mymusic.PlayActivity;
import com.qzl.mymusic.R;
import com.qzl.mymusic.adapter.MyMusicListAdapter;
import com.qzl.mymusic.application.CodingkePlayerApp;
import com.qzl.mymusic.serivice.PlayService;
import com.qzl.mymusic.utils.MediaUtil;
import com.qzl.mymusic.vo.Mp3Info;

import java.util.ArrayList;
/**
 * A simple {@link Fragment} subclass.
 */
public class MyMusicListFragment extends Fragment implements AdapterView.OnItemClickListener,View.OnClickListener{

    private ListView listView_myMusic;
    private ImageView imageView_album;
    private TextView textView_songName,textView2_singer;
    private ImageView imageView2_play_pause,imageView3_next;
    private QuickScroll quickScroll;
    private ArrayList<Mp3Info> mp3Infos;

    private MyMusicListAdapter myMusicListAdapter;
    private MainActivity mainActivity;

    private int position = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    //进行实例化
    public static MyMusicListFragment newInstance() {
        MyMusicListFragment my = new MyMusicListFragment();
        return my;
    }

    //用来加载布局
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_music_list_layout, null);
        listView_myMusic = (ListView) view.findViewById(R.id.listView_myMusic);
        imageView_album = (ImageView) view.findViewById(R.id.imageView_album);
        imageView2_play_pause = (ImageView) view.findViewById(R.id.imageView2_play_pause);
        imageView3_next = (ImageView) view.findViewById(R.id.imageView3_next);
        quickScroll  = (QuickScroll) view.findViewById(R.id.qs);

        textView_songName = (TextView) view.findViewById(R.id.textView_songName);
        textView2_singer = (TextView) view.findViewById(R.id.textView2_singer);

        listView_myMusic.setOnItemClickListener(this);
        imageView2_play_pause.setOnClickListener(this);
        imageView3_next.setOnClickListener(this);
        imageView_album.setOnClickListener(this);
        //不能在这儿调运，绑定之后才能加载数据,可以在MainActivity中调用
        //loadDate();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //绑定播放服务
        System.out.println("myMusicListFragment onresume...");
        mainActivity.bindPlayService();
    }

    @Override
    public void onPause() {
        super.onPause();
        //解绑播放服务
        System.out.println("myMusicListFragment onPause...");
        mainActivity.unbindPlayService();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mainActivity.unbindPlayService();
    }

    /**
     * 加载本地音乐列表
     */
    public void loadDate() {
        //涉及同步问题，就不能自己去查询了
        mp3Infos = MediaUtil.getMp3Infos(mainActivity);
       // mp3Infos = mainActivity.playService.mp3Infos;
        myMusicListAdapter = new MyMusicListAdapter(mainActivity,mp3Infos);
        listView_myMusic.setAdapter(myMusicListAdapter);

        initQuickScroll();
    }

    private void initQuickScroll() {
        quickScroll.init(QuickScroll.TYPE_POPUP_WITH_HANDLE, listView_myMusic, myMusicListAdapter, QuickScroll.STYLE_HOLO);
        quickScroll.setFixedSize(1);
        quickScroll.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 48);//方框的大小
        quickScroll.setPopupColor(QuickScroll.BLUE_LIGHT, QuickScroll.BLUE_LIGHT_SEMITRANSPARENT, 1, Color.WHITE,1);
    }

    //listView里面点击的方法
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mainActivity.playService.getChangePlayList() != PlayService.MY_MUSIC_LIST){
            mainActivity.playService.setMp3Infos(mp3Infos);
            mainActivity.playService.setChangePlayList(PlayService.MY_MUSIC_LIST);
        }
        //点那个播放那个
        mainActivity.playService.play(position);

        //保存播放时间
        savePlayRecord();
    }

    private void savePlayRecord() {
        Mp3Info mp3Info = mainActivity.playService.getMp3Infos().get(mainActivity.playService.getCurrentPosition());
        try{
            Mp3Info playRecordMp3Info = CodingkePlayerApp.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId", "=",mp3Info.getId()));
            if(playRecordMp3Info == null){
                mp3Info.setMp3InfoId(mp3Info.getId());
                mp3Info.setPlayTime(System.currentTimeMillis());//设置当前播放时间
                CodingkePlayerApp.dbUtils.save(mp3Info);
            }else{
                playRecordMp3Info.setPlayTime(System.currentTimeMillis());
                CodingkePlayerApp.dbUtils.update(playRecordMp3Info, "playTime");
            }
        }catch(DbException e){
            e.printStackTrace();
        }
    }

    //更新UI:回调播放状态下的UI设置
    public void changeUIStatusOnPlay(int position){
        if(position >= 0 && position < mainActivity.playService.mp3Infos.size()){
            Mp3Info mp3Info = mainActivity.playService.mp3Infos.get(position);
            textView_songName.setText(mp3Info.getTitle());
            textView2_singer.setText(mp3Info.getArtist());
            if(mainActivity.playService.isPlaying()){
                imageView2_play_pause.setImageResource(R.mipmap.pause);
            }else {
                imageView2_play_pause.setImageResource(R.mipmap.play);
            }
            //获取专辑图片
            Bitmap albumBitmap = MediaUtil.getAtWork(mainActivity, mp3Info.getId(), mp3Info.getAlbumId(), true, true);
            imageView_album.setImageBitmap(albumBitmap);

            this.position = position;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imageView2_play_pause: {
                //播放按钮的实现
                if (mainActivity.playService.isPlaying()) {
                    imageView2_play_pause.setImageResource(R.mipmap.play);
                    mainActivity.playService.pause();
                } else {
                    if (mainActivity.playService.isPause()) {
                        imageView2_play_pause.setImageResource(R.mipmap.pause);
                        mainActivity.playService.start();
                    } else {
                        mainActivity.playService.play(mainActivity.playService.getCurrentPosition());//0表示从第一首播放
                    }
                }
                break;
            }
            case R.id.imageView3_next:{
                //播放下一首
                mainActivity.playService.next();
                break;
            }
            case R.id.imageView_album: {
                Intent intent = new Intent(mainActivity,PlayActivity.class);
                startActivity(intent);
                break;
            }
            default:
                break;
        }
    }
}
