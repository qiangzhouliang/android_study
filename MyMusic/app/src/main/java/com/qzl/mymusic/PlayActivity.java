package com.qzl.mymusic;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.qzl.mymusic.application.CodingkePlayerApp;
import com.qzl.mymusic.serivice.PlayService;
import com.qzl.mymusic.utils.Constant;
import com.qzl.mymusic.utils.DownloadUtils;
import com.qzl.mymusic.utils.MediaUtil;
import com.qzl.mymusic.utils.SearchMusicUtils;
import com.qzl.mymusic.vo.Mp3Info;
import com.qzl.mymusic.vo.SearchResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import douzi.android.view.DefaultLrcBuilder;
import douzi.android.view.ILrcBuilder;
import douzi.android.view.ILrcView;
import douzi.android.view.LrcRow;
import douzi.android.view.LrcView;

import static com.qzl.mymusic.utils.DownloadUtils.*;

public class PlayActivity extends BaseActivity implements View.OnClickListener,SeekBar.OnSeekBarChangeListener{

    private TextView textView1_title,tv_end_time,tv_start_time;
    private ImageView imageView1_album,imageView_play_mode,iv_prev,imageView2_play_pause,imageView1_next,imageView1_favorite;
    private SeekBar seekBar1;

    private ViewPager viewPager;
    private LrcView lrc_view;
    private ArrayList<View> views = new ArrayList<View>();
    //private ArrayList<Mp3Info> mp3Infos;
    private boolean  isPause = false;
    private static final int UPDATE_TIME = 0x1;//设置更新播放时间的一个标记
    private static final int UPDATE_LRC = 0x2;
    private CodingkePlayerApp app;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);

        app = (CodingkePlayerApp) getApplication();
        tv_end_time = (TextView) findViewById(R.id.tv_end_time);
        tv_start_time = (TextView) findViewById(R.id.tv_start_time);
        //textView1_title = (TextView) findViewById(R.id.textView1_title);
        //imageView1_album = (ImageView) findViewById(R.id.imageView1_album);
        imageView_play_mode = (ImageView) findViewById(R.id.imageView_play_mode);
        iv_prev = (ImageView) findViewById(R.id.iv_prev);
        imageView2_play_pause = (ImageView) findViewById(R.id.imageView2_play_pause);
        imageView1_next = (ImageView) findViewById(R.id.imageView1_next);
        imageView1_favorite = (ImageView) findViewById(R.id.imageView1_favorite);

        seekBar1 = (SeekBar) findViewById(R.id.seekBar1);
        initViewPager();

        //事件注册
        imageView2_play_pause.setOnClickListener(this);
        imageView1_next.setOnClickListener(this);
        iv_prev.setOnClickListener(this);
        imageView_play_mode.setOnClickListener(this);
        imageView1_favorite.setOnClickListener(this);
        seekBar1.setOnSeekBarChangeListener(this);

        //mp3Infos = MediaUtil.getMp3Infos(this);

        myHandler = new MyHandler(this);
        isPause = getIntent().getBooleanExtra("isPause",false);
    }

    private void initViewPager() {
        viewPager = (ViewPager) findViewById(R.id.vp);
        View album_image_layout = getLayoutInflater().inflate(R.layout.album_image_layout, null);
        imageView1_album = (ImageView) album_image_layout.findViewById(R.id.imageView1_album);
        textView1_title = (TextView) album_image_layout.findViewById(R.id.textView1_title);
        views.add(album_image_layout);

        View lrcLayout = getLayoutInflater().inflate(R.layout.lrc_layout, null);
        lrc_view = (LrcView) lrcLayout.findViewById(R.id.lrc_view);//这里粗心漏写lrcLayout,报了空指针异常
        //设置滚动事件
        lrc_view.setListener(new ILrcView.LrcViewListener() {

            @Override
            public void onLrcSeeked(int newPosition, LrcRow row) {
                if (playService.isPlaying()) {
                    playService.seekTo((int) row.time);
                }
            }
        });
        lrc_view.setLoadingTipText("为卓音悦 因你动听......！！！");
        lrc_view.setBackgroundResource(R.drawable.jb_bg);
		lrc_view.getBackground().setAlpha(150);//透明度

        views.add(lrcLayout);
        viewPager.setAdapter(new MyPagerAdapter());
        //viewPager.addOnPageChangeListener((ViewPager.OnPageChangeListener) this);
    }

    class MyPagerAdapter extends PagerAdapter{

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0==arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager)container).removeView(views.get(position));
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(views.get(position),0);
            return views.get(position);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        //绑定服务
        bindPlayService();
        System.out.println("服务已绑定");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //解除绑定
        unbindPlayService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindPlayService();
    }

    private static MyHandler myHandler ;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //进度条的变化
        if(fromUser){
            playService.pause();
            playService.seekTo(progress);
            playService.start();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    static class MyHandler extends Handler {

        private PlayActivity playActivity;
        public MyHandler(PlayActivity playActivity){
            this.playActivity = playActivity;
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(playActivity != null){
                switch (msg.what){
                    case UPDATE_TIME://更新时间
                        playActivity.tv_start_time.setText(MediaUtil.formatTime(msg.arg1));
                        break;
                    case UPDATE_LRC:
                        playActivity.lrc_view.seekLrcToTime(msg.arg1);
                        break;
//                    case DownloadUtils.SUCCESS_LRC:
//                        playActivity.loadLRC(new File((String) msg.obj));
//                        break;
//                    case DownloadUtils.FAILED_LRC:
//                        Toast.makeText(playActivity, "歌词下载失败", Toast.LENGTH_SHORT).show();
//                        break;
                }
            }
        }
    }

    //实时更新
    @Override
    public void publish(int progress) {
        Message msg = myHandler.obtainMessage(UPDATE_TIME);
        msg.arg1 = progress;
        myHandler.sendMessage(msg);
        //tv_start_time.setText(MediaUtil.formatTime(progress));
        seekBar1.setProgress(progress);

        msg = myHandler.obtainMessage(UPDATE_LRC);
        msg.arg1 = progress;
        myHandler.sendMessage(msg);

//		myHandler.obtainMessage(UPDATE_LRC,progress).sendToTarget();
    }

    //改变
    @Override
    public void change(int position) {
            Mp3Info mp3Info = playService.mp3Infos.get(position);
            textView1_title.setText(mp3Info.getTitle());
            //获取专辑图片
            Bitmap albumBitmap = MediaUtil.getAtWork(this, mp3Info.getId(), mp3Info.getAlbumId(), true, false);//false表示取大图
            imageView1_album.setImageBitmap(albumBitmap);
            tv_end_time.setText(MediaUtil.formatTime(mp3Info.getDuration()));//结束时间
            seekBar1.setProgress(0);//当前进度
            seekBar1.setMax((int) mp3Info.getDuration());//总进度

            if(playService.isPlaying()){
                imageView2_play_pause.setImageResource(R.mipmap.pause);
            }else {
                imageView2_play_pause.setImageResource(R.mipmap.play);
            }

        switch (playService.getPlay_mode()){
            case PlayService.ORDER_PLAY:
            //playService.ORDER_PLAY:
                imageView_play_mode.setImageResource(R.mipmap.order);
                imageView_play_mode.setTag(playService.ORDER_PLAY);
                break;
            case PlayService.RANDOM_PLAY:
                //playService.RANDOM_PLAY:
                imageView_play_mode.setImageResource(R.mipmap.random);
                imageView_play_mode.setTag(playService.RANDOM_PLAY);
                break;
            case PlayService.SINGLE_PLAY:
                //playService.SINGLE_PLAY:
                imageView_play_mode.setImageResource(R.mipmap.single);
                imageView_play_mode.setTag(playService.SINGLE_PLAY);
                break;
            default:
                break;
        }
        //初始化收藏状态
        try {
            Mp3Info likeMp3Info = app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId","=",mp3Info.getMp3InfoId()));
            if(likeMp3Info != null){
                imageView1_favorite.setImageResource(R.mipmap.xin_hong);
            }else {
                imageView1_favorite.setImageResource(R.mipmap.xin_bai);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }

        //歌词
        String songName = mp3Info.getTitle();
        String lrcPath = Environment.getExternalStorageDirectory()+ Constant.DIR_LRC+"/"+songName+".lrc";
        File lrcFile = new File(lrcPath);
			if(!lrcFile.exists()){
				//下载歌词
				try {
					SearchMusicUtils.getInstance().setListener(new SearchMusicUtils.onSearchResultLisener() {

						@Override
						public void onSearchResult(ArrayList<SearchResult> results) {
                            if(results != null) {
                                SearchResult searchResult = results.get(0);
                                String url = Constant.BAIDU_URL + searchResult.getUrl();
                                try {
                                    getInstance().downloadLRC(url, searchResult.getMusicName(), myHandler);
                                } catch (ParserConfigurationException e) {
                                    e.printStackTrace();
                                }
                            }
						}
					}).search(songName+" "+mp3Info.getArtist(),1);
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				}
			}else{
            loadLRC(lrcFile);
			}

        // }
    }

    private long getId(Mp3Info mp3Info){
        //初始化收藏状态
        long id = 0;
        switch(playService.getChangePlayList()){
            case PlayService.MY_MUSIC_LIST:
                id = mp3Info.getId();
                break;
            case PlayService.MY_LIKE_MUSIC_LIST:
                id = mp3Info.getMp3InfoId();
                break;
            default:
                break;
        }
        return id;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imageView2_play_pause: {
                if (playService.isPlaying()) {
                    imageView2_play_pause.setImageResource(R.mipmap.play);//暂停状态
                    playService.pause();
                } else {
                    if (playService.isPause()) {
                        imageView2_play_pause.setImageResource(R.mipmap.pause);
                        playService.start();
                    } else {
                        playService.play(playService.getCurrentPosition());//0表示从第一首播放
                    }
                }
                break;
            }
            case R.id.imageView1_next:{
                playService.next();
                break;
            }
            case R.id.iv_prev:{
                //上一首
                playService.prev();
                break;
            }

            case R.id.imageView_play_mode: {
                int mode = (int) imageView_play_mode.getTag();
                switch (mode){
                    case PlayService.ORDER_PLAY:
                        imageView_play_mode.setImageResource(R.mipmap.random);
                        imageView_play_mode.setTag(playService.RANDOM_PLAY);
                        playService.setPlay_mode(playService.RANDOM_PLAY);
                        Toast.makeText(PlayActivity.this,getString(R.string.random_play),Toast.LENGTH_SHORT).show();
                        break;
                    case PlayService.RANDOM_PLAY:
                        imageView_play_mode.setImageResource(R.mipmap.single);
                        imageView_play_mode.setTag(playService.SINGLE_PLAY);
                        playService.setPlay_mode(playService.SINGLE_PLAY);
                        Toast.makeText(PlayActivity.this,getString(R.string.single_play),Toast.LENGTH_SHORT).show();
                        break;case PlayService.SINGLE_PLAY:
                        imageView_play_mode.setImageResource(R.mipmap.order);
                        imageView_play_mode.setTag(playService.ORDER_PLAY);
                        playService.setPlay_mode(playService.ORDER_PLAY);
                        Toast.makeText(PlayActivity.this,getString(R.string.order_play),Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
            }
            //收藏按钮
            case R.id.imageView1_favorite: {
                //得到当前这首歌的对象
                Mp3Info mp3Info = playService.mp3Infos.get(playService.getCurrentPosition());
                System.out.println("mp3Info.getId()"+mp3Info.getId());
                try {
                    Mp3Info likeMp3Info = app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId","=",getId(mp3Info)));
                    System.out.println("likeMp3Info"+likeMp3Info);
                    if(likeMp3Info == null){
                        //如果没有喜欢的时候
                        mp3Info.setMp3InfoId(mp3Info.getId());//先存一下
                        mp3Info.setIsLike(1);
                        app.dbUtils.save(mp3Info);
                        imageView1_favorite.setImageResource(R.mipmap.xin_hong);
                        System.out.println("save");
                    }else {
                        //如果有
                        int isLike = likeMp3Info.getIsLike();
                        if(isLike == 1){
                            likeMp3Info.setIsLike(0);
                            imageView1_favorite.setImageResource(R.mipmap.xin_bai);
                        }else {
                            likeMp3Info.setIsLike(1);
                            imageView1_favorite.setImageResource(R.mipmap.xin_hong);
                        }
                        app.dbUtils.update(likeMp3Info,"isLike");
                        System.out.println("update");
                    }
                } catch (DbException e) {
                    e.printStackTrace();
                }
                break;
            }
            default:
                break;
        }
    }

    /**
     * 加载歌词
     */
    private void loadLRC(File lrcFile){
        StringBuffer buf = new StringBuffer(1024*10);
        char[] chars = new char[1024];
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(lrcFile)));
            int len = -1;
            while((len = in.read(chars))!=-1){
                buf.append(chars,0,len);
            }
            in.close();
        }catch(FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ILrcBuilder builder = new DefaultLrcBuilder();
        List<LrcRow> rows = builder.getLrcRows(buf.toString());
        lrc_view.setLrc(rows);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_lrc:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.mipmap.music);
                builder.setTitle("说明");
                builder.setMessage("如果要加载歌词，只需把歌词文件（名称为：歌名.lrc）拷贝到SdCard下的weitayinyue_music/lrc（没有就自己新建一个文件夹）下，就可以读取歌词了.\n注意：如果歌手显示unknown，那么整个音乐名称即为歌名，对应歌词文件命名要写对。\n歌词下载网：www.lrcgc.com");
                builder.setCancelable(true);
                builder.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
