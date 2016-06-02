package com.qzl.mymusic.adapter;



import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.qzl.mymusic.R;
import com.qzl.mymusic.vo.SearchResult;

import java.util.ArrayList;

public class NetMusicListAdapter extends BaseAdapter{

	private Context context;
	private ArrayList<SearchResult> searchResults;
	
	public ArrayList<SearchResult> getSearchResults() {
		return searchResults;
	}


	public void setSearchResults(ArrayList<SearchResult> searchResults) {
		this.searchResults = searchResults;
	}


	public NetMusicListAdapter(Context context,ArrayList<SearchResult> searchResults){
		this.context = context;
		this.searchResults = searchResults;
	}
	
	
	@Override
	public int getCount() {
		return searchResults.size();
	}

	@Override
	public Object getItem(int position) {
		return searchResults.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh;
		if(convertView==null){
			convertView = LayoutInflater.from(context).inflate(R.layout.net_item_music_list, null);
			vh = new ViewHolder();
			vh.tv_title = (TextView) convertView.findViewById(R.id.tv_item_title);
			vh.tv_singer = (TextView) convertView.findViewById(R.id.tv_item_singer);
			convertView.setTag(vh);
		}
		vh = (ViewHolder) convertView.getTag();
		SearchResult searchResult = searchResults.get(position);
		vh.tv_title.setText(searchResult.getMusicName());
		vh.tv_singer.setText(searchResult.getArtist());
		return convertView;
	}

	static class ViewHolder{
		TextView tv_title;
		TextView tv_singer;
	}


}
