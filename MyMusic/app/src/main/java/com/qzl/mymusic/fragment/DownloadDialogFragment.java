package com.qzl.mymusic.fragment;

import com.qzl.mymusic.MainActivity;
import com.qzl.mymusic.R;
import com.qzl.mymusic.utils.DownloadUtils;
import com.qzl.mymusic.vo.SearchResult;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import java.io.File;

import javax.xml.parsers.ParserConfigurationException;

public class DownloadDialogFragment extends DialogFragment{

	private SearchResult searchResult;//当前要下在的歌曲对象
	private MainActivity mainActivity;
	
	public static DownloadDialogFragment newInstance(SearchResult searchResult){
		DownloadDialogFragment downloadDialogFragment = new DownloadDialogFragment();
		downloadDialogFragment.searchResult = searchResult;
		return downloadDialogFragment;
	}
	
	private String[] items;
	
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mainActivity = (MainActivity) getActivity();
		items = new String[]{"下载","取消"};
	}

	//创建对话框的事件方法
	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
		builder.setCancelable(true);//可以按返回键取消
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch(which){
				case 0:
					//执行下载
					//downloadMusic();
					AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
					builder.setIcon(R.mipmap.music);
					builder.setTitle("说明");
					builder.setMessage("百度音乐已经不支持下载，因此只提供加载列表功能");
					builder.setCancelable(true);
					builder.setNeutralButton("我知道了", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					builder.show();
					break;
				case 1:
					dialog.dismiss();
					break;
				default:
					break;
				}
			}
		});
		return builder.show();
	}

	protected void downloadMusic() {
		Toast.makeText(mainActivity, "正在下载： " + searchResult.getMusicName(), Toast.LENGTH_SHORT).show();
		try {
			DownloadUtils.getInstance().setListener(new DownloadUtils.onDownloadListener(){

				@Override
				public void onDownload(String mp3url) {
					Toast.makeText(mainActivity, "歌曲下载成功", Toast.LENGTH_SHORT).show();

					//扫描新下载歌曲
					Uri contentUri = Uri.fromFile(new File(mp3url));
					Intent meidaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,contentUri);
					getContext().sendBroadcast(meidaScanIntent);
				}

				@Override
				public void onFailed(String error) {
					Toast.makeText(mainActivity, "歌曲下载失败", Toast.LENGTH_SHORT).show();
				}

			}).download(searchResult);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
}
