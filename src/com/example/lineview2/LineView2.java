/**
 * Copyright (C) 2014 Soyea System Inc.
 * This file write by whw in 2018-1-16,mail: wanghw@soyea.com.cn
 */

package com.example.lineview2;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 * 支持滚动的lineView
 * 
 * @author whw
 * @version V1.0
 */

public class LineView2 extends ViewGroup {
	
	/** 超过50个站分两行*/
	public final static int MAX_ITEMS = 50;

	public LineView2(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
	}

	public LineView2(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}

	public LineView2(Context context) {
		super(context);
	}
	

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childCount = getChildCount();
		Log.e("-----", "----childCount--->"+childCount);
		
		if(childCount <=0){
			return;
		}
		WindowManager wm = (WindowManager) getContext()
				.getSystemService(Context.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();
		int height = wm.getDefaultDisplay().getHeight();

		if(childCount <= MAX_ITEMS){
			int childwidth = width/childCount;
			
			//得到子view的最大高度
			int maxChildHeight = 0;
			
			//子view最小高度,一半至少.居中
			int minChildHeight = height / 2;
			int paddingBottom = minChildHeight/2;
			int paddingTop = minChildHeight/2;
			
			for(int i = 0; i<childCount;i++){
				View view = getChildAt(i);
				view.measure(childwidth | MeasureSpec.EXACTLY, height | MeasureSpec.AT_MOST);
				maxChildHeight = Math.max(maxChildHeight, view.getMeasuredHeight());
			}
			
			if(maxChildHeight >= height){
				//那么上下边；
				paddingBottom = 25;
				paddingTop = 5;
				
			}else if(maxChildHeight > minChildHeight){
				paddingBottom = (height - maxChildHeight)/2;
				paddingTop = paddingBottom;
			}
			
			for(int i = 0; i<childCount;i++){
				View view = getChildAt(i);
				view.measure(childwidth | MeasureSpec.EXACTLY, height-paddingBottom-paddingTop | MeasureSpec.EXACTLY);
					
				//浮点计算有误差的，放左右两边
				int padding = (width - childwidth * childCount)/2;
				
				view.layout((i*childwidth)+padding, 0+paddingTop, (i+1)*childwidth+padding, height-paddingBottom);
				((ChildStationView)view).updateType();
				
				if(i == 0){
					((ChildStationView)view).updateType(1, 1);
				}else if(i == childCount -1){
					((ChildStationView)view).updateType(1, 3);
				}else {
					((ChildStationView)view).updateType();
				}
				
			}
		}else{
			int singleCount = (childCount/2+childCount%2);	
			int childwidth = width/singleCount;	
			int childHeight = height/2;
			
			int padding = (width - childwidth * singleCount)/2;
			
			for(int i = 0; i<childCount;i++){
				View view = getChildAt(i);
				view.measure(childwidth | MeasureSpec.EXACTLY, childHeight-3 | MeasureSpec.EXACTLY);
				
				if(i <singleCount){
					view.layout(i*childwidth+padding, 3, (i+1)*childwidth+padding, childHeight);
					if(i == 0){
						((ChildStationView)view).updateType(1, 1);
					}else if(i == singleCount-1){
						((ChildStationView)view).updateType(1, 4);
					}else {
						((ChildStationView)view).updateType();
					}
				}else{
					view.layout((singleCount - (i- singleCount+1))*childwidth + padding, 
							childHeight, 
							(singleCount - (i- singleCount))*childwidth+padding, 
							height - 3);
					
					((ChildStationView)view).updateType(2, 2);
					if(i == singleCount){
						((ChildStationView)view).updateType(2, 5);
					}else if(i == childCount-1){
						((ChildStationView)view).updateType(2, 1);
					}else {
						((ChildStationView)view).updateType();
					}
				}
				
			}
		}
				
	}
	
	private int mCurIndex = 0;
	public void setCurSelectIndex(int index){
		mCurIndex = index;		
		int childCount = getChildCount();
		for(int i = 0 ; i < childCount;i++){
			ChildStationView view = (ChildStationView) getChildAt(i);
			if(i < mCurIndex){		
				view.setCurState(ChildStationView.STATE_PASSED);
			}else if(i == mCurIndex){
				view.setCurState(ChildStationView.STATE_SELECTED);
			}else{
				view.setCurState(ChildStationView.STATE_NORMAL);
			}
		}
	}
	
	private List<String> mStationList = new ArrayList<String>();
	public void updateStationList(List<String> list){

		Log.e("-----", "----list--->"+list.size());
		//为了老屏而适配
		boolean scaleTextSize = false;
		WindowManager wm = (WindowManager) getContext()
				.getSystemService(Context.WINDOW_SERVICE);
		int height = wm.getDefaultDisplay().getHeight();

		Log.e("-----", "----height--->"+height);
		if(height == 520 && list != null && list.size() > MAX_ITEMS){
			int maxTextLenght = 0;
			for(String station:list){
				maxTextLenght = Math.max(station.length(), maxTextLenght);
			}
			
			//老屏，并且最多只有6个汉字的话，就缩小一下字体
			scaleTextSize = (maxTextLenght >= 6);
		}
		
		if(list != null){
			mStationList = list;
			removeAllViews();
			
			for(String station:mStationList){
				ChildStationView view = new ChildStationView(getContext());
				
				//为了老屏适配而增加的
				if(scaleTextSize){
					view.setTextSize(28);
				}
				
				view.setStationName(station);
				addView(view);
			}
		}
		
	}
	
	

}
