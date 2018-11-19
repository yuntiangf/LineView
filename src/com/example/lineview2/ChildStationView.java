/**
 * Copyright (C) 2014 Soyea System Inc.
 * This file write by whw in 2018-1-16,mail: wanghw@soyea.com.cn
 */

package com.example.lineview2;


import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;

import com.example.lineview.R;

/**
 * 站点显示的子view
 *@author		whw	
 *@version		V1.0
 *
 *类型1
 *		A B	C D E F G
 *		o-o-o-o-o-o-o
 ********************
 *类型2
 *		A B	C D E F G
 *		o-o-o-o-o-o-o┐
 *		o-o-o-o-o-o-o┘
 *		N M L K J I H
 */
public class ChildStationView extends View{
	
	public final static String TAG = "ChildStationView";
	
	/**
	 * 1文字在指示上
	 * 		 A
	 * 		-o-
	 */
	public final static int TEXT_DIRECT_UP = 1;
	
	/**
	 * 2文字在指示下
	 * 		-o-
	 * 		 A
	 */
	public final static int TEXT_DIRECT_DOWN = 2;
	
	/**
	 * 圆圈不含左边线条Item
	 * 	o-
	 */
	public final static int TYPE_1 = 1;
	
	/**
	 * 中间的item,左右都有
	 * -o-
	 */
	public final static int TYPE_2 = 2;
	
	/**
	 * 不含右边线条的item
	 *  -o
	 */
	public final static int TYPE_3 = 3;
	
	/**
	 * 右边右下转弯的item
	 * 	o┐
	 */
	public final static int TYPE_4 = 4;
	
	/**
	 * 右边右上转弯的item
	 * 	o┘
	 */
	public final static int TYPE_5 = 5;
	
	/**
	 * 状态 正常，当前，已过去。
	 */
	public final static int STATE_NORMAL = 1;
	public final static int STATE_SELECTED = 2;
	public final static int STATE_PASSED = 3;
	
	private int mPassedColor;
	private int mSelectColor;
	
	/**
	 * 文字的方向
	 */
	private int mCurTextDir = 1;
	
	/**
	 * 指示器类型
	 */
	private int mCurIndicationType = 1;
	
	/**
	 * 当前的站点名--文字
	 */
	public String mStationText = "";
	
	
	//====画画相关====//
	private Paint mPaint;
	public final static int CIRCLE_RADIU = 6;//dp小圆圈半径
	public final static int CIRCLE_LINE_STORKEWIDTH = 3;//dp线条宽度
	public final static int STATION_TEXT_SIZE = 30;//sp
	public final static int CIRCLE_LINE_COLOR = Color.RED;//线条颜色
	
	public final static int LINE_MARGIN = 10;//dp指示器 上下距离
	public final static int RIGHT_CORNERS = 5;//dp指示器  右半圆半径
	
	private int mCircleRadiu;//px单位
	private int mCircleStrokeWidth;//px单位
	private int mStationTextSize;//px单位
	private int mLineMargin;//px单位
	private int mRightCorners;//px单位
	
	private RectF mArcRectF;
	
	private boolean mNeedScroll = false;
	private int mNeedScrollHeight = 0;
	private float translateY = 0;//滚动的距离
	private RectF mTextRectF;
	
	private RectF mStateRectF;
	
	private int mState = 1;
	
	private int mSingleTextHeight = 31;
	
	private int width = 0;
	private int height = 0;
	public ChildStationView(Context context) {
		super(context);
		init();
	}
	
	public ChildStationView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public ChildStationView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init(){

		Log.e("-----", "----init--->");
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Style.STROKE);
		mPaint.setTypeface(Typeface.DEFAULT_BOLD);
		
		mCircleRadiu = DpUtil.dp2px(getContext(), CIRCLE_RADIU);
		mCircleStrokeWidth = DpUtil.dp2px(getContext(), CIRCLE_LINE_STORKEWIDTH);
		mStationTextSize = DpUtil.sp2px(getContext(), STATION_TEXT_SIZE);
		mLineMargin = DpUtil.dp2px(getContext(), LINE_MARGIN);
		mRightCorners = DpUtil.dp2px(getContext(), RIGHT_CORNERS);
		
		mPaint.setTextSize(mStationTextSize);		
		
		mSingleTextHeight = getTextInfo("国",mPaint).height();
		
		mArcRectF = new RectF();
		mTextRectF = new RectF();
		mStateRectF = new RectF();
		
		mPassedColor = getResources().getColor(R.color.beforeColor);
		mSelectColor = getResources().getColor(R.color.laterColor);
		
	}
	
	
	/**
	 * 为了适配而增加的接口
	 *@param textSize
	 */
	public void setTextSize(int textSize){
		
		mStationTextSize = textSize;
		mPaint.setTextSize(mStationTextSize);	
		mSingleTextHeight = getTextInfo("国",mPaint).height();
	}
	
	/**
	 * 设置站点名字
	 *@param stationName 名字
	 *@param textDir 文字方向
	 *@param type 指示器类型
	 */
	public void setStationName(String stationName,int textDir,int type){
		mStationText = formateString(stationName);
		mCurTextDir = textDir;
		mCurIndicationType = type;
		invalidate();
		needSrollIf();
	}
	
	public void setStationName(String stationName){
		Log.e("-----", "----setStationName--->"+stationName);
		setStationName(stationName,1,2);
	}
	
	public void updateType(int textDir,int type){
		mCurTextDir = textDir;
		mCurIndicationType = type;		
		invalidate();		
		needSrollIf();
	}
	
	public void updateType(){	
		needSrollIf();
	}
	
	public void setCurState(int state){
		mState = state;
		invalidate();
	}
	
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		// TODO Auto-generated method stub
		super.onDetachedFromWindow();
	}

	/**
	 * 
	 * 判断是否要滚动
	 */
	private void needSrollIf(){
//		Log.e("----", "----needSrollIf---");
		if(height == 0){
			return;
		}
		
		if(mStationText != null && mStationText.length() > 0){
			
			int singleTextHeight = mSingleTextHeight * 10 / 9;
			int textHeight = (mStationText.length()) * singleTextHeight; 
						
			if(textHeight > (getHeight() - mLineMargin)){
				
				mNeedScrollHeight = textHeight - (getHeight() - mLineMargin);
				mNeedScrollHeight = (mNeedScrollHeight/singleTextHeight+1) * singleTextHeight;
				
				int time = (mNeedScrollHeight/singleTextHeight)*2000+2000;
				
				startAni(-singleTextHeight, mNeedScrollHeight+singleTextHeight, time);
			}else{
				if(animator != null){
					animator.cancel();
				}
				
				translateY = 0;
			}
		}else {
			
			if(animator != null){
				animator.cancel();
			}
			
			translateY = 0;
		}		
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
//		super.onDraw(canvas);

		Log.e("-----", "----onDraw--->");
		//先得到宽高
		int width = getWidth();
		int height = getHeight();
		Log.e("-----", "----width--->"+width+"----height--->"+height);
		
		//先画指示的
		float cx,cy;
		if(mCurTextDir == TEXT_DIRECT_UP){//文字在上
			cx = width/2;
			cy = height - mLineMargin;
		}else {//文字在下
			 cx = width/2;
			 cy = mLineMargin;	
		}
					
		canvas.save();
		mPaint.setStyle(Style.FILL);
		if(mCurTextDir == TEXT_DIRECT_UP){//文字在上
			drawText4(canvas,mStationText,cx,height-mLineMargin/2,mPaint);				
		}else {//文字在下
			drawText3(canvas,mStationText,cx,mLineMargin/2,mPaint);		
		}		
		
		canvas.restore();
		
		
		mPaint.setStyle(mState == STATE_NORMAL ? Style.STROKE : Style.FILL);
		mPaint.setColor(CIRCLE_LINE_COLOR);
		mPaint.setStrokeWidth(mCircleStrokeWidth);

		if(mCurIndicationType == TYPE_1){
			canvas.drawLine(cx+mCircleRadiu, cy, width, cy, mPaint);
		}else if(mCurIndicationType == TYPE_2){
			canvas.drawLine(0,cy,cx-mCircleRadiu, cy, mPaint);
			canvas.drawLine(cx+mCircleRadiu, cy, width, cy, mPaint);
		}else if(mCurIndicationType == TYPE_3){
			canvas.drawLine(0,cy,cx-mCircleRadiu, cy, mPaint);
		}else if(mCurIndicationType == TYPE_4){
			canvas.drawLine(0,cy,cx-mCircleRadiu, cy, mPaint);
			mArcRectF.set(cx-mLineMargin,cy,cx+mLineMargin,cy+mLineMargin*2);
			mPaint.setStyle(Style.STROKE);
			canvas.drawArc(mArcRectF, 295, 90, false,mPaint);
		}else if(mCurIndicationType == TYPE_5){
			canvas.drawLine(0,cy,cx-mCircleRadiu, cy, mPaint);
			mArcRectF.set(cx-mLineMargin,cy-mLineMargin*2,cx+mLineMargin,cy);
			mPaint.setStyle(Style.STROKE);
			canvas.drawArc(mArcRectF, 0, 65, false,mPaint);
		}
		
		mPaint.setStyle(mState == STATE_NORMAL ? Style.STROKE : Style.FILL);
		canvas.drawCircle(cx, cy, mCircleRadiu, mPaint);
			
	}
	
	 ValueAnimator animator;
	 private void startAni(final float f1,final float f2,long time){
		 		 	
	        if(animator != null){
	            if(animator.isRunning()){
	                animator.cancel();
	            }
	        }
	        
	        animator = ValueAnimator.ofFloat(f1, f2);
	        animator.setDuration(time);
	        animator.setRepeatCount(-1);
	        animator.setInterpolator(new ADADInterpolator());
	        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
	            @Override
	            public void onAnimationUpdate(ValueAnimator animation) {
	                
	            	float value = (Float)animation.getAnimatedValue();

	            	if(value < 0){
	            		value = 0;
	            	}
	            	if(value > mNeedScrollHeight){
	            		value = mNeedScrollHeight;
	            	}
	            
	               translateY = value;
	                invalidate();
	            }
	        });
	        animator.start();
	    }
    
	/** 	 
	 * 文字在下
	 */
   private void drawText3(Canvas canvas,String text,float x, float y,Paint paint){
       if(text != null){
           text = formateString(text);       
           paint.setColor(Color.BLACK);

           y += mSingleTextHeight/2;
           
           //测量每个文字的宽度
           float[] widths = new float[text.length()];
           paint.getTextWidths(text, widths);
           
           float size = paint.getTextSize();
           if(mState != STATE_NORMAL){
          	 //背景的颜色
                 mStateRectF.set(x - (size/2)-2, 
              		   y-2, x + (size/2)+2, 
              		   getHeight());
                 
                 mPaint.setColor(mState == STATE_PASSED?mPassedColor : mSelectColor);
                 canvas.drawRoundRect(mStateRectF, 2, 2, mPaint);
             }
             
             mPaint.setColor(mState == STATE_SELECTED ?Color.WHITE : Color.BLACK);
             
           mTextRectF.set(0, y, getWidth(), getHeight());
           canvas.clipRect(mTextRectF);
           y -= translateY;
           
               for(int i = 0; i< text.length(); i++){
                   String s = text.substring(i,i+1); 
                   	  if(i == 0){
                   		 y += (mSingleTextHeight);
                   	  }else {
                   		 y += (mSingleTextHeight*10/9);
                   	  }
                      canvas.drawText(s,x - (widths[i]/2),y,paint);              
               }
       }
   }
   
   /**
    * 文字在上 
    */
   private void drawText4(Canvas canvas,String text,float x, float y,Paint paint){
       if(text != null){
           text = formateString(text);
           paint.setColor(Color.BLACK);
           
           //测量文字的高度  
           mTextRectF.set(0, 0, getWidth(), y - mSingleTextHeight/2); 
           
           //测量每个文字的宽度
           float[] widths = new float[text.length()];
           paint.getTextWidths(text, widths);
           
           float size = paint.getTextSize();
           if(mState != STATE_NORMAL){
        	 //背景的颜色
        	   mStateRectF.set(x - (size/2) - 2, 
            		   0, x + (size/2)+ 2, 
            		   y - mSingleTextHeight/2 + 2);
               
               mPaint.setColor(mState == STATE_PASSED?mPassedColor : mSelectColor);
               canvas.drawRoundRect(mStateRectF, 3, 3, mPaint);
           }
           
           mPaint.setColor(mState == STATE_SELECTED ?Color.WHITE : Color.BLACK);
           y+= (mSingleTextHeight/2);                
           canvas.clipRect(mTextRectF);
           y += translateY;
           
           for(int i = text.length(); i>0; i--){
               String s = text.substring(i-1,i);       
                  y -= (mSingleTextHeight*10/9);
                  canvas.drawText(s,x - (widths[i-1]/2),y,paint);
               
           }
       }
   }
   
   private Rect getTextInfo(String text, Paint paint) {
       Rect mrect = new Rect();
       //获得文本的最小矩形大小,也是测量文本高度,宽度的一种方法
       paint.getTextBounds(text, 0, text.length(), mrect);
       return mrect;
   }
   
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		Log.e("-----", "---->"+measureMyWidth(widthMeasureSpec)+"------>"+measureMyHeight(heightMeasureSpec));
		width = measureMyWidth(widthMeasureSpec);
		height = measureMyHeight(heightMeasureSpec);

        setMeasuredDimension(measureMyWidth(widthMeasureSpec),
        		measureMyHeight(heightMeasureSpec));
    }

    private int measureMyWidth(int measureSpec) {
        int result=0;
        //获取当前View的测量模式
        int mode = MeasureSpec.getMode(measureSpec);
        //精准模式获取当前Viwe测量后的值,如果是最大值模式,会获取父View的大小.
        int size = MeasureSpec.getSize(measureSpec);
        if (mode==MeasureSpec.EXACTLY){
            //当测量模式为精准模式,返回设定的值
            result=size;
        }else{
            //设置为WrapContent的默认大小
//            result= (int)bus_line_width;
            if (mode==MeasureSpec.AT_MOST){
                //当模式为最大值的时候,默认大小和父类View的大小进行对比,返回最小的值
                result= Math.max(result,size);
            }
        }
        return result;
    }
    
    private int measureMyHeight(int measureSpec) {
        int result=0;
        
		if(mStationText != null && mStationText.length() > 0){
			
			int singleTextHeight = mSingleTextHeight * 10 / 9;
			int textHeight = (mStationText.length()) * singleTextHeight; 			
			result = textHeight + mLineMargin * 2;
		}
        
        //获取当前View的测量模式
        int mode = MeasureSpec.getMode(measureSpec);
        //精准模式获取当前Viwe测量后的值,如果是最大值模式,会获取父View的大小.
        int size = MeasureSpec.getSize(measureSpec);
        if (mode==MeasureSpec.EXACTLY){
            //当测量模式为精准模式,返回设定的值
            result=size;
        }else if (mode==MeasureSpec.AT_MOST){
            //当模式为最大值的时候,默认大小和父类View的大小进行对比,返回最小的值
            result= Math.min(result,size);
        }
        
        return result;
    }
    
	private String formateString(String stationName){

        if(stationName != null){
            return stationName.replace("(", "︵").replace("（", "︵").replace(")", "︶").replace("）", "︶")
                    .replace("●", " ●").replace("·", " ●").replace("・", " ●");
        }

        return "";
    }

	public class ADADInterpolator implements Interpolator {
	    public ADADInterpolator() {
	    }
	    
	    @SuppressWarnings({"UnusedDeclaration"})
	    public ADADInterpolator(Context context, AttributeSet attrs) {
	    }
	    
	    public float getInterpolation(float input) {
	        return (float)(Math.cos((2* input + 1) * Math.PI) / 2.0f) + 0.5f;
	    }
	}

	
	
	
}
