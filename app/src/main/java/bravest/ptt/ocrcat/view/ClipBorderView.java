package bravest.ptt.ocrcat.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import bravest.ptt.ocrcat.R;
import bravest.ptt.ocrcat.utils.DensityUtil;

public class ClipBorderView extends View {

    private static final String TAG = "ClipBorderView";

    public static final int ACTION_TOP_DRAG = 1;
    public static final int ACTION_BOTTOM_DRAG = 2;
    public static final int ACTION_CENTER_DRAG = 3;

    private Context mContext;

    // 边框线条颜色
    private int mBorderColor = Color.parseColor("#FFFFFF");
    // 边框宽度
    private int mBorderWidth = 2;
    // 边框画笔
    private Paint mBoarderPaint;

    // 中心矩形颜色
    private int mRectColor = Color.parseColor("#20000000");
    // 中心矩形画笔
    private Paint mRectPaint;
    // 矩形宽度
    private int mWidth = 0;
    // 矩形高度
    private int mHeight = 0;
    private int mMinHeight = 0;
    private int mMaxHeight = 0;
    // 中心矩形
    private RectF mRectF;

    // 包裹拖拽按钮的矩形，用来确定是否点击了拖拽按钮
    private RectF[] mDragRectFs = new RectF[2];
    // 矩形相对于拖拽图的padding
    private final int mDragRectPadding = 40;
    // 平行线上拖拽的按钮
    private Bitmap mDragBitmap;
    //拖拽按钮对于平行线的偏移量
    private int mDragBitmapOffset;
    private int mDragAction = ACTION_CENTER_DRAG;
    private float mDragActionY = 0;

    private static int SCREEN_W = 0;
    private static int SCREEN_H = 0;

    private OnClipperRectListener mOnClipperRectListener;

    public interface OnClipperRectListener {
        void onRectChanged(int deltaY);
    }

    public ClipBorderView(Context context) {
        super(context);
        initView(context);
    }

    public ClipBorderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ClipBorderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        SCREEN_W = DensityUtil.getDeviceWidth(mContext);
        SCREEN_H = DensityUtil.getDeviceHeight(mContext);

        mMinHeight = SCREEN_H / 12;
        mMaxHeight = SCREEN_H / 2;

        mBoarderPaint = new Paint();
        mBoarderPaint.setColor(mBorderColor);
        mBoarderPaint.setStrokeWidth(mBorderWidth);
        mBoarderPaint.setStyle(Paint.Style.STROKE);

        mRectPaint = new Paint();
        mRectPaint.setColor(mRectColor);
        mRectPaint.setAntiAlias(true);
        mRectPaint.setStyle(Paint.Style.FILL);

        mDragBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_crop_drag_y);
        mDragBitmapOffset = mDragBitmap.getWidth() / 2;

        if (mRectF == null) {
            int left = 0;
            int top = 0;
            int right = SCREEN_W;
            int bottom = getMinHeight() - mDragBitmapOffset;
            mRectF = new RectF(left, top, right, bottom);
        }
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
//        int width = MeasureSpec.getSize(widthMeasureSpec);
//
//        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);
//        int height = MeasureSpec.getSize(heightMeasureSpec);

//        int width = SCREEN_W;
//        int height = getMyHeight(heightMeasureSpec);
//        Log.d(TAG, "onMeasure: widht = " + width + ", height = " + height);
//        setMeasuredDimension(width, height);
//    }

    private int getMyHeight(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;//确切大小,所以将得到的尺寸给view
        } else if (specMode == MeasureSpec.AT_MOST) {
            //默认值为450px,此处要结合父控件给子控件的最多大小(要不然会填充父控件),所以采用最小值
            result = (int) Math.min(mRectF.height(), specSize);
        } else {
            result = (int) mRectF.height();
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // init rect width = Screen width
        if (mWidth == 0) mWidth = SCREEN_W;
        // init rect height = Screen height / 6
        if (mHeight == 0) mHeight = (SCREEN_H / 6);
        canvas.save();

        drawLine(canvas);
        drawRect(canvas);
        drawIcon(canvas);

        canvas.restore();
        super.onDraw(canvas);
    }

    private void drawLine(Canvas canvas) {
        // draw top line
        canvas.drawLine(0, mRectF.top, SCREEN_W, mRectF.top, mBoarderPaint);
        // draw bottom line
        canvas.drawLine(0, mRectF.bottom, SCREEN_W, mRectF.bottom, mBoarderPaint);
    }

    private void drawRect(Canvas canvas) {
        canvas.drawRect(mRectF, mRectPaint);
    }

    private void drawIcon(Canvas canvas) {
        float dragLeft = SCREEN_W / 2 - mDragBitmapOffset;

        //canvas.drawBitmap(mDragBitmap, dragLeft, mRectF.top - mDragBitmapOffset, null);
        canvas.drawBitmap(mDragBitmap, dragLeft, mRectF.bottom - mDragBitmapOffset, null);

        RectF rectF = new RectF(
                dragLeft - mDragRectPadding,
                mRectF.top - mDragBitmapOffset - mDragRectPadding,
                dragLeft + mDragRectPadding,
                mRectF.top + mDragBitmapOffset + mDragRectPadding
        );
        mDragRectFs[0] = rectF;

        rectF = new RectF(
                dragLeft - mDragRectPadding,
                mRectF.bottom - mDragBitmapOffset - mDragRectPadding,
                dragLeft + mDragRectPadding,
                mRectF.bottom + mDragBitmapOffset + mDragRectPadding
        );
        mDragRectFs[1] = rectF;
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        final float x = event.getRawX();
//        final float y = event.getRawY();
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                if (mDragRectFs[0].contains(x, y)) {
//                    mDragAction = ACTION_TOP_DRAG;
//                } else if (mDragRectFs[1].contains(x, y)) {
//                    mDragAction = ACTION_BOTTOM_DRAG;
//                } else {
//                    mDragAction = ACTION_CENTER_DRAG;
//                }
//                mDragActionY = y;
//                break;
//            case MotionEvent.ACTION_MOVE:
//                float yDelta = mDragActionY - y;
//                switch (mDragAction) {
//                    case ACTION_TOP_DRAG:
//                        // top线拉到0
//                        if (mRectF.top - yDelta <= 0) {
//                            mRectF.top = 0;
//                            break;
//                        }
//                        if (mRectF.bottom - (mRectF.top - yDelta) <= mMinHeight) {
//                            mRectF.top = mRectF.bottom - mMinHeight;
//                            break;
//                        }
//                        if (mRectF.bottom - (mRectF.top - yDelta) >= mMaxHeight) {
//                            mRectF.top = mRectF.bottom - mMaxHeight;
//                            break;
//                        }
//                        mRectF.top = mRectF.top - yDelta;
//                        if (mOnClipperRectListener != null)
//                            mOnClipperRectListener.onRectChanged((int) mRectF.top);
//                        break;
//                    case ACTION_BOTTOM_DRAG:
//                        if (mRectF.bottom - yDelta >= SCREEN_H) {
//                            mRectF.bottom = SCREEN_H;
//                            break;
//                        }
//                        if (mRectF.bottom - yDelta - mRectF.top <= mMinHeight) {
//                            mRectF.bottom = mRectF.top + mMinHeight;
//                            break;
//                        }
//                        if (mRectF.bottom - yDelta - mRectF.top >= mMaxHeight) {
//                            mRectF.bottom = mRectF.top + mMaxHeight;
//                            break;
//                        }
//                        mRectF.bottom = mRectF.bottom - yDelta;
//                        if (mOnClipperRectListener != null)
//                            mOnClipperRectListener.onRectChanged((int) mRectF.top);
//                        break;
//                    case ACTION_CENTER_DRAG:
//                        if (yDelta > 0) {
//                            if (mRectF.top - yDelta <= 0) {
//                                mRectF.top = 0;
//                                break;
//                            } else {
//                                mRectF.top = mRectF.top - yDelta;
//                            }
//                            if (mRectF.bottom - yDelta >= SCREEN_H) {
//                                mRectF.bottom = SCREEN_H;
//                                break;
//                            } else {
//                                mRectF.bottom = mRectF.bottom - yDelta;
//                            }
//                        } else {
//                            if (mRectF.bottom - yDelta >= SCREEN_H) {
//                                mRectF.bottom = SCREEN_H;
//                                break;
//                            } else {
//                                mRectF.bottom = mRectF.bottom - yDelta;
//                            }
//                            if (mRectF.top - yDelta <= 0) {
//                                mRectF.top = 0;
//                                break;
//                            } else {
//                                mRectF.top = mRectF.top - yDelta;
//                            }
//                        }
//                        if (mOnClipperRectListener != null)
//                            mOnClipperRectListener.onRectChanged((int) mRectF.top);
//                        break;
//                    default:
//                        break;
//                }
//                mDragActionY = y;
//                invalidate();
//                break;
//            case MotionEvent.ACTION_UP:
//                mDragAction = -1;
//                break;
//        }
//        return true;
//    }

    public void setOnClipperRectListener(OnClipperRectListener listener) {
        mOnClipperRectListener = listener;
    }

    public Point getLeftTopPoint() {
        return new Point((int)mRectF.left, (int)mRectF.top);
    }

    public int getClipperWidth() {
        return (int) (mRectF.right - mRectF.left);
    }

    public int getClipperHeight() {
        return (int) (mRectF.bottom - mRectF.top);
    }

    public RectF getTopDragRectF() {
        return mDragRectFs[0];
    }

    public RectF getBottomDragRectF() {
        return mDragRectFs[1];
    }

    public void setDragAction(int action) {
        mDragAction = action;
    }

    public int getDragAction() {
        return mDragAction;
    }

    public int getMinHeight() {
        return mMinHeight;
    }

    public int getMaxHeight() {
        return mMaxHeight;
    }

    public RectF getRectF() {
        return mRectF;
    }

    public int getDragBitmapOffset() {
        return mDragBitmapOffset;
    }
}
