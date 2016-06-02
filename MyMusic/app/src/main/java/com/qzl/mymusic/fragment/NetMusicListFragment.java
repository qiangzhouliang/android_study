package com.qzl.mymusic.fragment;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.qzl.mymusic.MainActivity;
import com.qzl.mymusic.R;
import com.qzl.mymusic.adapter.NetMusicListAdapter;
import com.qzl.mymusic.utils.AppUtils;
import com.qzl.mymusic.utils.Constant;
import com.qzl.mymusic.utils.SearchMusicUtils;
import com.qzl.mymusic.vo.SearchResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;


/**
 * A simple {@link Fragment} subclass.
 */
public class NetMusicListFragment extends Fragment implements View.OnClickListener,AdapterView.OnItemClickListener{


    private MainActivity mainActivity;
    private ListView lv_net_music;
    private LinearLayout ll_load;
    private LinearLayout ll_search_btn_container;
    private LinearLayout ll_search_container;
    private ImageButton ib_search_btn;
    private EditText et_search;
    private ArrayList<SearchResult> searchResults = new ArrayList<SearchResult>();
    private NetMusicListAdapter netMusicListAdapter;
    private int page = 1;//显示搜索的页码
    //进行实例化
    public static NetMusicListFragment newInstance() {
        NetMusicListFragment net = new NetMusicListFragment();
        return net;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //UI组件的初始化
        View view = inflater.inflate(R.layout.net_music_list_layout, null, false);
        lv_net_music = (ListView) view.findViewById(R.id.lv_net_music);
        ll_load = (LinearLayout) view.findViewById(R.id.ll_load);
        ll_search_btn_container = (LinearLayout) view.findViewById(R.id.ll_search_btn_container);
        ll_search_container = (LinearLayout) view.findViewById(R.id.ll_search_container);
        ib_search_btn = (ImageButton) view.findViewById(R.id.ib_search_btn);
        et_search = (EditText) view.findViewById(R.id.et_search);

        lv_net_music.setOnItemClickListener(this);
        ll_search_btn_container.setOnClickListener(this);
        ib_search_btn.setOnClickListener(this);
        loadNetData();//加载网络音乐
        return view;
    }

    private void loadNetData() {
        ll_load.setVisibility(View.VISIBLE);//显示
        //执行异步加载网络音乐的任务
        new LoadNetDataTask().execute(Constant.BAIDU_URL+ Constant.BAIDU_DAYHOT);
    }
    /**
     * @author Administrator
     *加载网络音乐的异步任务
     */
    class LoadNetDataTask extends AsyncTask<String, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //让一个显示，一个不显示
            ll_load.setVisibility(View.VISIBLE);
            lv_net_music.setVisibility(View.GONE);
            searchResults.clear();
        }

        @Override
        protected Integer doInBackground(String... params) {
            String url = params[0];
            try{
                //使用Jsoup组件请求网络，并解析音乐数据
                Document doc = Jsoup.connect(url).userAgent(Constant.USER_AGENT).timeout(6*1000).get();
				//System.out.println(doc);
                Elements songTitles = doc.select("span.song-title");//歌名
                Elements artists = doc.select("span.author_list");//歌手列表
                for(int i=0;i<songTitles.size()-1;i++){
                    SearchResult searchResult = new SearchResult();
                    Elements urls = songTitles.get(i).getElementsByTag("a");//获取里面的a连接
                    searchResult.setUrl(urls.get(0).attr("href"));
                    searchResult.setMusicName(urls.get(0).text());//获取歌名

                    Elements artistElements = artists.get(i).getElementsByTag("a");//获取歌手
                    searchResult.setArtist(artistElements.get(0).text());//取第一个a连接的（从doc中分析得到）

                    searchResult.setAlbum("热歌榜");
                    searchResults.add(searchResult);
                }
                //System.out.println(searchResults);
            }catch(IOException e){
                e.printStackTrace();
                return -1;
            }

            return 1;
        }


        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if(result==1){
                netMusicListAdapter = new NetMusicListAdapter(mainActivity,searchResults);
//				System.out.println(searchResults);
                lv_net_music.setAdapter(netMusicListAdapter);
                lv_net_music.addFooterView(LayoutInflater.from(mainActivity).inflate(R.layout.footview_layout, null));
            }
            ll_load.setVisibility(View.GONE);
            lv_net_music.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.ll_search_btn_container:
                ll_search_btn_container.setVisibility(View.GONE);
                ll_search_container.setVisibility(View.VISIBLE);
                break;
            case R.id.ib_search_btn:
                //搜素事件处理
                searchMusic();
                break;
            default:
                break;
        }
    }

    //搜索音乐
    private void searchMusic() {
        //隐藏输入法
        AppUtils.hideInputMethod(et_search);
        ll_search_btn_container.setVisibility(View.VISIBLE);
        ll_search_container.setVisibility(View.GONE);
        String key = et_search.getText().toString();
        if(TextUtils.isEmpty(key)){
            Toast.makeText(mainActivity, "请输入歌名或歌手", Toast.LENGTH_SHORT).show();
            return;
        }
        ll_load.setVisibility(View.VISIBLE);

        try {
            SearchMusicUtils.getInstance().setListener(new SearchMusicUtils.onSearchResultLisener() {

                @Override
                public void onSearchResult(ArrayList<SearchResult> results) {
                    ArrayList<SearchResult> sr = netMusicListAdapter.getSearchResults();
                    sr.clear();
                    sr.addAll(results);
                    netMusicListAdapter.notifyDataSetChanged();
                    ll_load.setVisibility(View.GONE);
                }
            }).search(key, page);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    //列表项的单击事件
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(position >= netMusicListAdapter.getSearchResults().size() || position<0){
            return;
        }
        showDownloadDialog(position);
    }

    //下载弹窗
    private void showDownloadDialog(final int position) {
        DownloadDialogFragment downloadDialogFragment = DownloadDialogFragment.newInstance(searchResults.get(position));
        downloadDialogFragment.show(getFragmentManager(), "download");
    }
}
