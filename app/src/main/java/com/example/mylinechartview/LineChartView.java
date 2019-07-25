package com.example.mylinechartview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;


import java.util.ArrayList;
import java.util.List;

/**
 * 折线图：有阴影，虚线效果的
 */
public class LineChartView extends View {
    private Context mContext;
    public float mScreenWidth;
    public float mScreenHeight;
    public int COLUMN_COUNT = 6; // 列数
    public int ROW_COUNT = 5; // 行数

    public int START_X = 0;
    public float START_Y = 0;
    public final static int PADDING_TOP = 10;
    public final static int PADDING_BOTTOM = 10;

    private float mChartHeight; //表格高度
    private float mChartWidth; //表格宽度
    private float mItemWidth; //单元格宽度
    private float mItemHeight; //单元格高度
    private float mMaxData; //最大值
    private float mMinData; //最小值
    private float mAreaData; //差值

    private Bitmap mPointBitmap;
    private int mPointHeight; //小圆点高度

    private List<String> mDataList; //数据数组
    private List<String> mXCoordinateList; //x坐标数据数组
    private Paint mLinePaint;  //背景边框线
    private Paint mDataPaint;  //数据画笔
    private Paint mDataLinePaint;  //数据折线画笔
    private Paint mXPaint;  //横坐标文字的画笔
    private Paint mShaderPaint; //阴影画笔
    private Path mPath;
    private Path mShaderPath; //阴影边框连线
    private Shader mShader;

    //渐变阴影颜色数组
    private int[] shadeColors = new int[]{Color.parseColor("#33FF0000"), Color.parseColor("#3300FF00")};

    private String mXUnits = ""; //x坐标单位
    private String mYUnits = ""; //y坐标单位

    private String mItemData; // 获取的某个点的数据
    private float mItemDataNum; // 获取的某个点的数据

    public LineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initData();
    }

    public LineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initData();
    }

    /**
     * 初始化数据对象
     */
    private void initData() {
        DisplayMetrics metric = new DisplayMetrics();
        ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metric);
        mScreenWidth = metric.widthPixels; // 屏幕宽度（像素）
        mScreenHeight = metric.heightPixels; // 屏幕高度（像素）

        mDataList = new ArrayList<>();
        mChartWidth = mScreenWidth - dp2px(mContext,40);
        mItemHeight = dp2px(mContext,35);
        mChartHeight = mItemHeight*ROW_COUNT;
        START_X = dp2px(mContext,20);

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(1);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setColor(Color.parseColor("#999999"));
        //绘制长度为10的实线后再绘制长度为10的空白区域，起始位置10间隔
        mLinePaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 10));


        mDataPaint = new Paint();
        mDataPaint.setAntiAlias(true);
        mDataPaint.setTextSize(dp2px(mContext,12));
        mDataPaint.setColor(Color.parseColor("#111111"));

        mDataLinePaint = new Paint();
        mDataLinePaint.setStrokeWidth(dp2px(mContext,2));
        mDataLinePaint.setAntiAlias(true);
        mDataLinePaint.setStyle(Paint.Style.STROKE);
        mDataLinePaint.setColor(Color.parseColor("#3c7eef"));

        mXPaint = new Paint();
        mXPaint.setAntiAlias(true);
        mXPaint.setTextSize(dp2px(mContext,12));
        mXPaint.setColor(Color.parseColor("#999999"));

        mShaderPaint = new Paint();
        mShaderPaint.setAntiAlias(true);

        mPointBitmap = getBitmapFromDrawable(getResources().getDrawable(R.mipmap.ic_chart_point));
        mPointHeight = mPointBitmap.getHeight();

        mPath = new Path();
        mShaderPath = new Path();
    }

    public void setData(List<String> data, List<String> xCoordinateList) {
        mDataList = data;
        mXCoordinateList = xCoordinateList;
        COLUMN_COUNT = mDataList.size();
        mItemWidth = mChartWidth / (COLUMN_COUNT - 1);
        mMaxData = getMaxData(mDataList);
        mMinData = getMinData(mDataList);
        mAreaData = mMaxData - mMinData;

        invalidate();
    }

    private float getMaxData(List<String> datas) {
        float maxNum = 0;
        try {
            if (null != datas && datas.size() > 0) {
                for (int i = 0; i < mDataList.size(); i++) {
                    float num = Float.valueOf(mDataList.get(i));
                    if (num > maxNum) {
                        maxNum = num;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return maxNum;
    }

    private float getMinData(List<String> datas) {
        float minNum = 1000000;
        try {
            if (null != datas && datas.size() > 0) {
                for (int i = 0; i < mDataList.size(); i++) {
                    float num = Float.valueOf(mDataList.get(i));
                    if (num < minNum) {
                        minNum = num;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return minNum;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), (int) mChartHeight + dp2px(mContext,20)); // 加上底部文字的高度
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float areaHeight = mChartHeight * 3 / ROW_COUNT;
        float perHeight = 0;
        if (mAreaData == 0) {
            perHeight = 0.01f; // 可能出现除数为0.0的情况
        } else {
            perHeight = areaHeight / mAreaData; // 每个单位需要的高度
        }

        // 画横坐标
        mPath.reset();
        mPath.moveTo(START_X, START_Y + PADDING_TOP + mChartHeight);
        mPath.lineTo(START_X + mChartWidth, START_Y + PADDING_TOP + mChartHeight);
        canvas.drawPath(mPath, mLinePaint);

        // 画横线
        for (int i = 0; i < ROW_COUNT; i++) {
            mPath.reset();
            mPath.moveTo(START_X, START_Y + PADDING_TOP + mChartHeight - (i + 1) * mItemHeight);
            mPath.lineTo(START_X + mChartWidth, START_Y + PADDING_TOP + mChartHeight - (i + 1) * mItemHeight);
            canvas.drawPath(mPath, mLinePaint);
        }

        // 画竖线
        for (int i = 0; i < COLUMN_COUNT; i++) {
            mPath.reset();
            mPath.moveTo(START_X + mItemWidth * i, START_Y + PADDING_TOP);
            mPath.lineTo(START_X + mItemWidth * i, START_Y + mChartHeight + PADDING_TOP);
            canvas.drawPath(mPath, mLinePaint);
        }
        // 画数据点之间的连线
        mShaderPath.moveTo(START_X, START_Y + mChartHeight + PADDING_TOP);
        for (int i = 1; i < mDataList.size(); i++) {
            mItemDataNum = Float.valueOf(mDataList.get(i));
            float startX = START_X + mItemWidth * (i - 1);
            float stopX = START_X + mItemWidth * i;

            float startY = START_Y + mChartHeight - mItemHeight * 1 + PADDING_TOP
                    - ((Float.valueOf(mDataList.get(i - 1)) - mMinData) * perHeight);
            float stopY = START_Y + mChartHeight - mItemHeight * 1 + PADDING_TOP
                    - ((mItemDataNum - mMinData) * perHeight);
            mPath.reset();
            mPath.moveTo(startX, startY);
            mPath.lineTo(stopX, stopY);
            canvas.drawPath(mPath, mDataLinePaint);

            mShaderPath.lineTo(startX, startY);
        }
        //画填充阴影：从坐标(0,0)的点开始 --> 折线上的所有点 -- > (0,y) 画出一个多边形区域，然后在填充颜色
        mShaderPath.lineTo(START_X + mItemWidth * (COLUMN_COUNT - 1), START_Y + mChartHeight - mItemHeight * 1 + PADDING_TOP
                - ((Float.valueOf(mDataList.get(COLUMN_COUNT - 1)) - mMinData) * perHeight));
        mShaderPath.lineTo(START_X + mChartWidth, START_Y + mChartHeight + PADDING_TOP);

        mShader = new LinearGradient(0, 0, 0, mChartHeight, shadeColors, null, Shader.TileMode.CLAMP);
        mShaderPaint.setShader(mShader);

        canvas.drawPath(mShaderPath, mShaderPaint);


        // 画数据点
        for (int i = 0; i < mDataList.size(); i++) {
            mItemData = mDataList.get(i);
            mItemDataNum = Float.valueOf(mItemData);
            String textData = mItemDataNum + mYUnits;
            float textWidth = getTextWidthAndHeight(textData, mDataPaint)[0];
            float textHeight = getTextWidthAndHeight(textData, mDataPaint)[1];
            float stopX = START_X + mItemWidth * i;
            float stopY = START_Y + mChartHeight - mItemHeight * 1 + PADDING_TOP
                    - ((mItemDataNum - mMinData) * perHeight);
            canvas.drawBitmap(mPointBitmap, stopX - mPointHeight / 2, stopY
                    - mPointHeight / 2, mDataPaint);
            canvas.drawText(textData, stopX - textWidth / 2 - PADDING_TOP,
                    stopY - textHeight / 2 - PADDING_TOP, mDataPaint);
        }

        // 画月份
        for (int i = 0; i < mXCoordinateList.size(); i++) {
            String itemDate = mXCoordinateList.get(i) + mXUnits;
            float textWidth = getTextWidthAndHeight(itemDate, mXPaint)[0];
            float textHeight = getTextWidthAndHeight(itemDate, mXPaint)[1];
            float startX = START_X + mItemWidth * i;
            float startY = START_Y + PADDING_TOP + mChartHeight + PADDING_BOTTOM + textHeight;
            canvas.drawText(itemDate, startX - textWidth / 2, startY, mXPaint);
        }
    }

    //drawable转bitmap
    public Bitmap getBitmapFromDrawable(Drawable drawable) {
        Bitmap bitmap = Bitmap
                .createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    //获取字符串的宽高
    public float[] getTextWidthAndHeight(String str, Paint paint) {
        float[] widthAndHeight = {0, 0};
        if (str == null || str.length() <= 0) {
            return widthAndHeight;
        }
        Rect rect = new Rect();
        paint.getTextBounds(str, 0, str.length(), rect);
        widthAndHeight[0] = rect.width();
        widthAndHeight[1] = rect.height();
        return widthAndHeight;
    }

    private int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


}
