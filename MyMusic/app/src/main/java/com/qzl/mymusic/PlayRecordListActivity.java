package com.qzl.mymusic;


import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.qzl.mymusic.adapter.MyMusicListAdapter;
import com.qzl.mymusic.application.CodingkePlayerApp;
import com.qzl.mymusic.serivice.PlayService;
import com.qzl.mymusic.utils.Constant;
import com.qzl.mymusic.vo.Mp3Info;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PlayRecordListActivity extends BaseActivity implements OnItemClickListener{

	private ListView lv_record;
	private TextView tv_record;
	private CodingkePlayerApp app;
	private ArrayList<Mp3Info> mp3Infos;
	private MyMusicListAdapter adapter;
	@Override
	public void onCreate(@Nullable Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_play_record_music_list);
		app = (CodingkePlayerApp) getApplication();
		tv_record = (TextView) findViewById(R.id.tv_record);
		lv_record = (ListView) findViewById(R.id.lv_record);

		lv_record.setOnItemClickListener(this);
		initData();
	}
	//初始化最近播放的数据
	private void initData() {
		try {
			//查询最近播放的记录
			List<Mp3Info> list = app.dbUtils.findAll(Selector.from(Mp3Info.class).where("playTime", "!=", 0).orderBy("playTime", true).limit(Constant.PLAY_RECORD_NUM));
			if(list==null||list.size()==0){
				tv_record.setVisibility(View.VISIBLE);//温馨提示
				lv_record.setVisibility(View.GONE);
			}else{
				tv_record.setVisibility(View.GONE);
				lv_record.setVisibility(View.VISIBLE);
				mp3Infos = (ArrayList<Mp3Info>) list;
				adapter= new MyMusicListAdapter(this, mp3Infos);
				lv_record.setAdapter(adapter);
			}
		} catch (DbException e) {
			e.printStackTrace();
		}
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
	
	@Override
	public void publish(int progress) {
	}

	@Override
	public void change(int position) {
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if(playService.getChangePlayList()!= PlayService.PLAY_RECORD_MUSIC_LIST){
			playService.setMp3Infos(mp3Infos);
			playService.setChangePlayList(PlayService.PLAY_RECORD_MUSIC_LIST);
		}
		playService.play(position);
	}

}
