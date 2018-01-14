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
import android.view.MotionEvent;
import android.view.View;

import bravest.ptt.ocrcat.R;
import bravest.ptt.ocrcat.utils.DensityUtil;

public class ClipBorderView extends View {

    private static final int ACTION_TOP_DRAG = 1;
    private static final int ACTION_BOTTOM_DRAG = 2;
    private static final int ACTION_CENTER_DRAG = 3;

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
    // 矩形左上角顶点坐标
    private Point mLeftTopPoint = new Point();
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

        mMinHeight = SCREEN_H / 6;
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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mRectF == null) {
            int left = 0;
            int top = (SCREEN_H / 2) - (SCREEN_H / 12);
            int right = SCREEN_W;
            int bottom = (SCREEN_H / 2) + (SCREEN_H / 12);
            mRectF = new RectF(left, top, right, bottom);
        }
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

        canvas.drawBitmap(mDragBitmap, dragLeft, mRectF.top - mDragBitmapOffset, null);
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mDragRectFs[0].contains(event.getX(), event.getY())) {
                    mDragAction = ACTION_TOP_DRAG;
                } else if (mDragRectFs[1].contains(event.getX(), event.getY())) {
                    mDragAction = ACTION_BOTTOM_DRAG;
                } else {
                    mDragAction = ACTION_CENTER_DRAG;
                }
                mDragActionY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float y = mDragActionY - event.getY();
                switch (mDragAction) {
                    case ACTION_TOP_DRAG:
                        // top线拉到0
                        if (mRectF.top - y <= 0) {
                            mRectF.top = 0;
                            break;
                        }
                        if (mRectF.bottom - (mRectF.top - y) <= mMinHeight) {
                            mRectF.top = mRectF.bottom - mMinHeight;
                            break;
                        }
                        if (mRectF.bottom - (mRectF.top - y) >= mMaxHeight) {
                            mRectF.top = mRectF.bottom - mMaxHeight;
                            break;
                        }
                        mRectF.top = mRectF.top - y;
                        break;
                    case ACTION_BOTTOM_DRAG:
                        if (mRectF.bottom - y >= SCREEN_H) {
                            mRectF.bottom = SCREEN_H;
                            break;
                        }
                        if (mRectF.bottom - y - mRectF.top <= mMinHeight) {
                            mRectF.bottom = mRectF.top + mMinHeight;
                            break;
                        }
                        if (mRectF.bottom - y - mRectF.top >= mMaxHeight) {
                            mRectF.bottom = mRectF.top + mMaxHeight;
                            break;
                        }
                        mRectF.bottom = mRectF.bottom - y;
                        break;
                    case ACTION_CENTER_DRAG:
                        if (y > 0) {
                            if (mRectF.top - y <= 0) {
                                mRectF.top = 0;
                                break;
                            } else {
                                mRectF.top = mRectF.top - y;
                            }
                            if (mRectF.bottom - y >= SCREEN_H) {
                                mRectF.bottom = SCREEN_H;
                                break;
                            } else {
                                mRectF.bottom = mRectF.bottom - y;
                            }
                        } else {
                            if (mRectF.bottom - y >= SCREEN_H) {
                                mRectF.bottom = SCREEN_H;
                                break;
                            } else {
                                mRectF.bottom = mRectF.bottom - y;
                            }
                            if (mRectF.top - y <= 0) {
                                mRectF.top = 0;
                                break;
                            } else {
                                mRectF.top = mRectF.top - y;
                            }
                        }

                        break;
                    default:
                        break;
                }
                mDragActionY = event.getY();
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                mDragAction = -1;
                break;
        }
        return true;
    }
}
