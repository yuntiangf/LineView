package com.example.lineview;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Looper;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * 滑动需结合Scroll+HorizontalScrollView使用实现上下左右滑动)
 * 支持响应事件
 */
public class LineView extends AbsMyLineView implements GestureDetector.OnGestureListener{
    private List<String> data;
    /**
     * 圆点画笔
     */
    private Paint circlePaint;
    /**
     * 线段画笔
     */
    private Paint linePaint;
    /**
     * 文本画笔
     */
    private Paint textPaint;
    /**
     * 记录圆点路径集合
     */
    private Path[] circlePaths;
    /**
     * 记录线段路径集合
     */
    private Path[] linePaths;
    /**
     * 各个圆点中心点集合
     */
    private Point[] circlePoints;
    /**
     * view的宽度
     */
    private int mViewWidth;
    /**
     * 原点内圆半径
     */
    private int circleInnerRadius;

    /**
     * 线段长度(单位像素)
     */
    private float lineLength;
    /**
     * 笔触宽度(单位像素)
     */
    private int circleStokeWidth;
    /**
     * 原点半径:内圆半径+笔触宽度(单位:px)
     */
    private int circleRadius;
    /**
     * 原点顶部距离控件顶部距离(单位像素)
     */
    private int circleMarginTop;
    /**
     * 文本与圆点之间的距离(单位像素)
     */
    private int textMarginTop;
    /**
     * 文本与文本之间的距离(单位像素)
     */
    private int textSpace;
    /**
     * 之前站台的颜色
     */
    private int beforeColor;
    /**
     * 圆点颜色
     */
    private int circleColor;
    /**
     * 圆点选中颜色
     */
    private int circleSelectColor;
    /**
     * 线段颜色
     */
    private int lineColor;
    /**
     * 文本颜色
     */
    private int textColor;
    /**
     * 文本选中颜色
     */
    private int textSelectColor;
    /**
     * 线段笔触宽度
     */
    private int lineStrokeWidth;
    /**
     * 文本字体大小
     */
    private int textSize;
    /**
     * 文本高度
     */
    private int textHeight;
    /**
     * 记录每个条目的区域集合
     */
    private Region[] textRegions;
    /**
     * 默认选中第一条
     */
    private int currSelectedItem = 0;
    /**
     * 当前显示图标
     */
    private Bitmap bitmap;
    private int bitmapX;
    private int bitmapY;
    private int offsetX;

    private Bitmap bitmapArrows;

    float moveLength = 0;
    /**
	 * 更新公交车所在的位置
	 *@param busIndex 公交车数组。可能有多辆车在一条线上跑
	 */
    private int busIndex[];
    //2表示出站
    private int busIndex2[];

    public LineView(Context context) {
        this(context, null);
    }

    public LineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.LineView);
        try {
            circleColor = array.getColor(R.styleable.LineView_circle_color, getResources().getColor(R.color.circlr_green));
            lineColor = array.getColor(R.styleable.LineView_line_color, getResources().getColor(R.color.line_green));
            textColor = array.getColor(R.styleable.LineView_text_color, getResources().getColor(R.color.textColor_gray));
            circleStokeWidth = array.getDimensionPixelSize(R.styleable.LineView_circle_stroke, 4);
            lineStrokeWidth = array.getDimensionPixelSize(R.styleable.LineView_line_stroke, 4);
            textSize = (int) array.getDimension(R.styleable.LineView_text_textSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
            textSpace = array.getDimensionPixelSize(R.styleable.LineView_text_space, 5);
            textMarginTop = array.getDimensionPixelSize(R.styleable.LineView_text_marginTop, 20);
            circleMarginTop = array.getDimensionPixelSize(R.styleable.LineView_circle_marginTop, 40);
            circleInnerRadius = array.getDimensionPixelSize(R.styleable.LineView_circle_radius, 5);
            circleSelectColor = array.getColor(R.styleable.LineView_circle_select_color, getResources().getColor(R.color.selectColor));
            textSelectColor = array.getColor(R.styleable.LineView_text_select_color, getResources().getColor(R.color.selectColor));
            circleRadius = circleInnerRadius + circleStokeWidth / 2;
            mViewWidth = array.getDimensionPixelSize(R.styleable.LineView_view_width, 930);
            beforeColor = lineColor;
        } finally {
            array.recycle();
        }

        init();
    }

    private void init() {
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        circlePaint.setColor(circleColor);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(circleStokeWidth);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        linePaint.setColor(lineColor);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(lineStrokeWidth);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        textPaint.setColor(textColor);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setTextSize(textSize);

        setLongClickable(true);

        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.car);
        bitmap = getZoomImage(bitmap, 18, 18);
        bitmapArrows = BitmapFactory.decodeResource(getResources(), R.drawable.arrows);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //此处为与HorizontalScrollView搭配使用,达到滑动目的,别的滑动方式请修改此处代码或者删除
    	lineLength = computeLineLength();
    	textHeight = computeTextHeight();
//        int mViewWidth = getPaddingLeft() + getPaddingRight() + computeMinViewWidth();
        int mViewHeight = getPaddingBottom() + getPaddingTop() + computeNormalViewHeight();
        setMeasuredDimension(mViewWidth, mViewHeight);
    }
  
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        computeCircleAndLinePath();
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        if (textRegions == null || textRegions.length == 0) {
            return true;
        }
        for (int i = 0; i < textRegions.length; i++) {
            Region mPicArea = textRegions[i];
            if (mPicArea.contains((int) event.getX(), (int) event.getY())) {
                currSelectedItem = i;
                invalidateView();
            }
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    /**
     * 计算点圆与线轨迹
     */
    private void computeCircleAndLinePath() {
        if (data != null && data.size() != 0) {

            circlePoints = new Point[data.size()];
            circlePaths = new Path[data.size()];
            linePaths = new Path[data.size() - 1];

            //处理文字显示与圆点显示
            //绘制起始偏移量,如果文本高度一半大于半径,则取差值,否则取0
            offsetX = textHeight / 2 > (circleRadius + circleStokeWidth / 2) ? textHeight / 2 - (circleRadius + circleStokeWidth / 2) : 0;
            //处理paddingLeft
            offsetX += getPaddingLeft();
            for (int i = 0; i < data.size(); i++) {
                Path circlePath = new Path();
                //计算每个圆点的中心点
                int mCircleCenterX = (int) (i * lineLength + (2 * i) * circleRadius + circleStokeWidth / 2 + offsetX + bitmapArrows.getWidth());
                int mCircleCenterY = circleMarginTop + circleRadius;
                circlePoints[i] = new Point(mCircleCenterX, mCircleCenterY);

                //计算圆点路径
                circlePath.addCircle(mCircleCenterX, mCircleCenterY, circleRadius, Path.Direction.CCW);
                circlePaths[i] = circlePath;
                if (i == data.size() - 1) {
                    continue;
                }
                //计算线段路径
                Path linePath = new Path();
                //计算线段起始点
                int mlineStartX = mCircleCenterX + circleRadius + circleStokeWidth / 2 ;
                int mlineEndX = (int) (lineLength + mlineStartX - circleStokeWidth) +1;

                linePath.moveTo(mlineStartX, mCircleCenterY);
                linePath.lineTo(mlineEndX, mCircleCenterY);
                linePaths[i] = linePath;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
    	
    	try {
    		if (data != null && data.size() != 0) {
                drawCircleAndLine(canvas);
                drawItemText(canvas);
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
        
    }


    /**
     * 绘制文本
     */
    private void drawItemText(Canvas canvas) {
        //记录每个条目区域
        textRegions = new Region[data.size()];
        if(data.size() >= 32){
        	//单位px
        	textPaint.setTextSize(15);
        }else{
        	textPaint.setTextSize(18);
        }
        for (int i = 0; i < data.size(); i++) {
            Region region = new Region();
            int mItemStartX = circlePoints[i].x;
            int mItemStartY = circlePoints[i].y + circleRadius + textMarginTop;
            String text = formateString(data.get(i));
            char[] chars = text.toCharArray();
            //每个条目区域的计算
            Rect textArea = new Rect();
            textArea.left = mItemStartX - textHeight / 2;
            textArea.right = textArea.left + textHeight;
            //减去一个文字高度,因为绘制文字是在baseline上方绘制,基线位置为drawText(text,x,y,paint)的y位置
            textArea.top = mItemStartY - textHeight;
            int lastTextHeight = 0;
            //改变颜色
            if(i < currSelectedItem){
            	textPaint.setColor(textColor);
            }else if (i == currSelectedItem) {
                textPaint.setColor(textSelectColor);
            } else {
                textPaint.setColor(textColor);
            }
            if(chars.length <= 5){
            	for (int j = 0; j < chars.length; j++) {
            		canvas.drawText(String.valueOf(text.charAt(j)), mItemStartX - textHeight / 2, mItemStartY + lastTextHeight, textPaint);
            		lastTextHeight += textHeight + textSpace;
            	}
            }else{
            	//保存画布
            	canvas.save();
            	drawAnimItemText(canvas, text, mItemStartX - textHeight / 2, mItemStartY);
            	//恢复画布裁剪前的状态
            	canvas.restore();
            }
            //此处减去最后一个文字间隔
            textArea.bottom = textArea.top + lastTextHeight - textSpace;
            region.set(textArea);
            textRegions[i] = region;

        }
    }

    /**将需要动画的站点保存到hashMap里**/
    HashMap<String, Long> scrollerStations = new HashMap<String, Long>();
    
    /**
     * 文字过长时跑马灯显示文本
     */
    private void drawAnimItemText(Canvas canvas, String text, float width, float height) {
    	
	 	
    	if(scrollerStations != null){
    		if(!scrollerStations.containsKey(text)){
    			scrollerStations.put(text, SystemClock.elapsedRealtime());
    		}
    	}
    	
    		
        char[] chars = text.toCharArray();
        int lastTextHeight = 0;
        //基线的坐标上移距离
        int length =(chars.length -4) * (textHeight + textSpace);//预留多一点,前后各留一个字的时间拿来停顿
        //现在的时间距离开始绘画的时间,滚动5秒之内完成
        int shouldScrolltime = (chars.length-4)*2500;//每个字1500ms
        long costTime = SystemClock.elapsedRealtime() - scrollerStations.get(text);//从开始画到现在的间隔时间
        AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
        float temp = interpolator.getInterpolation((costTime%shouldScrolltime)/(float)shouldScrolltime) * length;
    	float offY = temp;
    	
		if(offY < 10){
    		offY = 0;
    	}else{
    		offY = offY - 10;
    	}

        Rect rect = new Rect((int)width,(int)(height - textHeight + textSpace),(int)(width + textHeight),(int)(height + (textHeight + textSpace)*4));  
        
        
    	for (int i = 0; i < chars.length; i++) {
    		//设置矩形裁剪区域
    		canvas.clipRect(rect);
    		canvas.drawText(String.valueOf(text.charAt(i)), width, height + lastTextHeight - offY, textPaint);
    		lastTextHeight += textHeight + textSpace;
    	}
    	
    	invalidate(rect);
//		invalidate();
    }

	/**
     * 计算文本信息,包含每个条目的宽度,高度
     */
    private Rect getTextInfo(String text, Paint paint) {
        Rect mrect = new Rect();
        //获得文本的最小矩形大小,也是测量文本高度,宽度的一种方法
        paint.getTextBounds(text, 0, text.length(), mrect);
        return mrect;
    }

    
    /**
     * 绘制圆和线段
     */
    private void drawCircleAndLine(Canvas canvas) {
        for (int i = 0; i < circlePaths.length; i++) {
        	//改变颜色与风格
        	if(busIndex != null){
        		for (int j = 0; j < busIndex.length; j++) {
    				if(i == busIndex[j] /*&& i <= currSelectedItem*/){
    					//当前圆心位置
    	                bitmapX = (int) (i * lineLength + (2 * i) * circleRadius + circleStokeWidth / 2 + offsetX + bitmapArrows.getWidth());
    	   			 	bitmapY = circleMarginTop;
    	                canvas.drawBitmap(bitmap, bitmapX - bitmap.getWidth() / 2, bitmapY - bitmap.getHeight() , null);
    				}		
            	}
        	}
        	
        	//加入出站显示(车辆出站，将图标显示在线段中间)
        	if(busIndex2 != null){
        		for (int j = 0; j < busIndex2.length; j++) {
    				if(i == busIndex2[j] && busIndex2[j] < data.size() - 1){
    					//当前圆心位置
    	                bitmapX = (int) (i * lineLength + (2 * i) * circleRadius + circleStokeWidth + offsetX + bitmapArrows.getWidth()
    	                		+ lineLength/2+circleRadius);
    	   			 	bitmapY = circleMarginTop;
    	                canvas.drawBitmap(bitmap, bitmapX - bitmap.getWidth() / 2, bitmapY - bitmap.getHeight() , null);
    				}		
            	}
        	}
    		
        	
    		if(i < currSelectedItem){
            	circlePaint.setStyle(Paint.Style.STROKE);
                circlePaint.setColor(circleColor);
            }else if (i == currSelectedItem) {
                circlePaint.setColor(circleSelectColor);
                circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
            }else {
                circlePaint.setStyle(Paint.Style.STROKE);
                circlePaint.setColor(circleColor);
            }
        		
            canvas.drawPath(circlePaths[i], circlePaint);
        }
        
        for (int i = 0; i < linePaths.length; i++) {
        	if(i < currSelectedItem){
        		linePaint.setColor(beforeColor);
        	}else{
        		linePaint.setColor(lineColor);
        	}
            canvas.drawPath(linePaths[i], linePaint);
        }
        //绘制箭头
        canvas.drawBitmap(bitmapArrows, 0, circleMarginTop - 2, null);
        canvas.drawBitmap(bitmapArrows, mViewWidth - getPaddingRight() - bitmapArrows.getWidth(), circleMarginTop - 2, null);
    }

    /**
     * 计算线段的长度
     */
    private float computeLineLength() {
        float length = 0;
        if (data != null && data.size() != 0) {
        	length = (mViewWidth - (data.size() * circleRadius * 2) - getPaddingLeft() - getPaddingRight() - 2 * bitmapArrows.getWidth() - 5) / (float)(data.size() - 1);
        }
        return length;
    }

    /**
     * 计算text初始高度
     */
    private int computeTextHeight() {
        if (data != null && data.size() != 0) {
            //计算文本高度,起始不管第一条是否为空,高度都会与其它文本保持一致
            Rect textRect = getTextInfo(data.get(0), textPaint);
            textHeight = textRect.height();
        }
        return textHeight;
    }
    
    /**
     * 计算View最小宽度
     */
    private int computeMinViewWidth() {
        int viewWidth = 0;
        if (data != null && data.size() != 0) {
            //计算文本高度,起始不管第一条是否为空,高度都会与其它文本保持一致
            Rect textRect = getTextInfo(data.get(0), textPaint);
            textHeight = textRect.height();
            viewWidth = (int) ((data.size() - 1) * lineLength + 2 * circleRadius + circleStokeWidth);
            if (textHeight > (2 * circleRadius + circleStokeWidth)) {
                int offsetX = textHeight / 2 - (circleRadius + circleStokeWidth / 2);
                viewWidth = viewWidth + 2 * offsetX;
            }
        }
        return viewWidth;
    }

    /**
     * 计算View最小高度
     */
    private int computeMinViewHeight() {
        int viewHeight = 0;
        if (data != null && data.size() != 0) {
            String maxText = "";
            for (int i = 0; i < data.size(); i++) {
                //计算文本宽度
                maxText = data.get(i).length() > maxText.length() ? formateString(data.get(i)) : maxText;
            }
            //获得总文本间距
            int textSpaceWidth = textSpace * (maxText.length() - 1);
            Rect textRect = getTextInfo(maxText, textPaint);
            //最大文本高度
            int textMaxHeight = textRect.width() + textSpaceWidth;
            //计算ViewHeight
            viewHeight = textMaxHeight + textMarginTop + (2 * circleRadius + circleStokeWidth) + circleMarginTop;
        }
        return viewHeight;
    }

    /**
     * 计算指定长度text文字的高度
     */
    private int computeNormalViewHeight() {
        int viewHeight = 0;
        if (data != null && data.size() != 0) {
            String maxText = "老电力大厦";
            //获得总文本间距
            int textSpaceWidth = textSpace * 4;
            Rect textRect = getTextInfo(maxText, textPaint);
            //最大文本高度
            int textMaxHeight = textRect.width() + textSpaceWidth;
            //计算ViewHeight
            viewHeight = textMaxHeight + textMarginTop + (2 * circleRadius + circleStokeWidth) + circleMarginTop;
        }
        return viewHeight;
    }
    
    /**
     * 图片的缩放方法
     *
     * @param orgBitmap ：源图片资源
     * @param newWidth  ：缩放后宽度
     * @param newHeight ：缩放后高度
     * @return
     */
    public static Bitmap getZoomImage(Bitmap orgBitmap, double newWidth, double newHeight) {
        if (null == orgBitmap) {
            return null;
        }
        if (orgBitmap.isRecycled()) {
            return null;
        }
        if (newWidth <= 0 || newHeight <= 0) {
            return null;
        }

        // 获取图片的宽和高
        float width = orgBitmap.getWidth();
        float height = orgBitmap.getHeight();
        // 创建操作图片的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(orgBitmap, 0, 0, (int) width, (int) height, matrix, true);
        return bitmap;
    }
    
    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
    	if(this.data == data){
    		return;
    	}
    	
        this.data = data;
        //若站台数过多，可以通过缩小文字大小来显示
        if(data.size() >= 32){
        	//单位px
        	textPaint.setTextSize(15);
        }else{
        	textPaint.setTextSize(18);
        }
        lineLength = computeLineLength();
    	textHeight = computeTextHeight(); 
        computeCircleAndLinePath();
        

        scrollerStations.clear();
        invalidateView();
    }

    interface OnLineViewItemSelectedistener {
        void onSelected(int position);
    }

    private void invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

	@Override
	public void updateLine(String LineName, List<String> stations) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateCurBus(int[] busIndex) {
		// TODO Auto-generated method stub
		this.busIndex = busIndex;
	}

	public void updateCurBus2(int[] busIndex) {
		// TODO Auto-generated method stub
		this.busIndex2 = busIndex;
	}

	@Override
	public void setCurStation(int stationIndex) {
		// TODO Auto-generated method stub
		this.currSelectedItem = stationIndex;
        invalidateView();
	}
	
	//括号等符号转换成竖直排列时显示
	private String formateString(String stationName){
		
		if(stationName != null){
			return stationName.replace("(", "︵").replace("（", "︵").replace(")", "︶").replace("）", "︶")
					.replace("●", " ●").replace("·", " ●").replace("・", " ●");
		}
		
		return "";
	}

}

