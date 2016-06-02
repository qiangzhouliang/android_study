package com.qzl.mymusic.utils;

import com.qzl.mymusic.application.CodingkePlayerApp;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class AppUtils {

	//隐藏输入法
	public static void hideInputMethod(View view){
		InputMethodManager imm = (InputMethodManager) CodingkePlayerApp.context.getSystemService(Context.INPUT_METHOD_SERVICE);
		if(imm.isActive()){
			imm.hideSoftInputFromInputMethod(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
	
}
