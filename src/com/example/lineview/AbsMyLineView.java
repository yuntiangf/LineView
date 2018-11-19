
package com.example.lineview;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public abstract class AbsMyLineView extends View{

	public AbsMyLineView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public AbsMyLineView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public AbsMyLineView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 更新线路列表
	 *@param LineName
	 *@param stations
	 */
	public abstract void updateLine(String LineName,List<String> stations);
	
	/**
	 * 更新公交车所在的位置
	 *@param busIndex 公交车数组。可能有多辆车在一条线上跑
	 */
	public abstract void updateCurBus(int[] busIndex);
	
	/**
	 * 这个公交站的站点--高亮显示
	 *@param stationIndex
	 */
	public abstract void setCurStation(int stationIndex);
		
}
