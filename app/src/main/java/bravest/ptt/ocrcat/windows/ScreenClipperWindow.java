package bravest.ptt.ocrcat.windows;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import bravest.ptt.ocrcat.R;
import bravest.ptt.ocrcat.utils.DensityUtil;
import bravest.ptt.ocrcat.view.ClipBorderView;
import bravest.ptt.ocrcat.windows.i.AbstractWindow;

import static bravest.ptt.ocrcat.view.ClipBorderView.ACTION_BOTTOM_DRAG;
import static bravest.ptt.ocrcat.view.ClipBorderView.ACTION_CENTER_DRAG;
import static bravest.ptt.ocrcat.view.ClipBorderView.ACTION_TOP_DRAG;

/**
 * Created by pengtian on 2018/1/14.
 */

public class ScreenClipperWindow extends AbstractWindow
        implements ClipBorderView.OnClipperRectListener, View.OnTouchListener {
    private static final String TAG = "ScreenClipperWindow";

    private static int SCREEN_H  = 0;
    private static int SCREEN_W = 0;

    private Context mContext;
    private WindowManager mWm;
    private WindowManager.LayoutParams mLayoutParams;
    private LayoutInflater mInflater;
    private ClipBorderView mClipper;

    public ScreenClipperWindow(Context context) {
        mContext = context;
        initVariables();
        initView();
        initLayoutParams();
    }

    @Override
    public void initVariables() {
        SCREEN_H = DensityUtil.getDeviceHeight(mContext);
        SCREEN_W = DensityUtil.getDeviceWidth(mContext);

        mWm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams();
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public void initView() {
        mInflater = LayoutInflater.from(mContext);
        if (mInflater == null) {
            return;
        }
        mView = mInflater.inflate(R.layout.layout_window_clipper, null);
        mClipper = mView.findViewById(R.id.clipper);
        mClipper.setOnTouchListener(this);
        //mClipper.setOnClipperRectListener(this);
    }

    @Override
    public void initLayoutParams() {
        mLayoutParams.flags = mLayoutParams.flags
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.dimAmount = 0.2f;
        mLayoutParams.type = Build.VERSION.SDK_INT < Build.VERSION_CODES.O ?
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY :
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        mLayoutParams.height = mClipper.getMinHeight();
        mLayoutParams.width = SCREEN_W;
        mLayoutParams.gravity = Gravity.START | Gravity.TOP;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.alpha = 1.0f;
        mLayoutParams.x = 0;
        mLayoutParams.y = SCREEN_H / 2 - SCREEN_H / 12;
    }

    @Override
    public void show() {
        if (!isShowing()) {
            mIsShowing = true;
        }
        if (mWm == null) {
            return;
        }
        mWm.addView(mView, mLayoutParams);
    }

    @Override
    public void hide() {
        if (mIsShowing) {
            mIsShowing = false;
            if (mWm == null) {
                return;
            }
            mWm.removeView(mView);
        }
    }

    @Override
    public void destroy() {
        // none
    }

    public void setVisible(boolean visible) {
        if (mView != null) {
            mView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public Point getLeftTopPoint() {
        if (mClipper != null) {
            return mClipper.getLeftTopPoint();
        }
        return null;
    }

    @Override
    public void onRectChanged(int top) {
        mLayoutParams.y = top;
        mLayoutParams.height = mClipper.getClipperHeight();
        Log.d(TAG, "onRectChanged: top = " + top + ", height = " + mClipper.getClipperHeight());
    }

    private float mDragDownY = 0;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final float rawY = event.getRawY();
        final float x = event.getX();
        final float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mClipper.getTopDragRectF().contains(x, y)) {
                    mClipper.setDragAction(ACTION_TOP_DRAG);
                } else if (mClipper.getBottomDragRectF().contains(x, y)) {
                    mClipper.setDragAction(ACTION_BOTTOM_DRAG);
                } else {
                    mClipper.setDragAction(ACTION_CENTER_DRAG);
                }
                mDragDownY = rawY;
                break;
            case MotionEvent.ACTION_MOVE:
                float yDelta = mDragDownY - rawY;
                switch (mClipper.getDragAction()) {
                    case ACTION_TOP_DRAG:
                        // top线拉到0
                        if (mLayoutParams.y - yDelta <= 0) {
                            mLayoutParams.y = 0;
                            break;
                        }
                        if (mLayoutParams.height - yDelta <= mClipper.getMinHeight()) {
                            mLayoutParams.y = mLayoutParams.y
                                    + mLayoutParams.height - mClipper.getMinHeight();
                            mLayoutParams.height = mClipper.getMinHeight();
                            break;
                        }
                        if (mLayoutParams.height - yDelta >= mClipper.getMaxHeight()) {
                            mLayoutParams.y = mLayoutParams.y
                                    + mLayoutParams.height - mClipper.getMaxHeight();
                            mLayoutParams.height = mClipper.getMaxHeight();
                            break;
                        }
                        mLayoutParams.y = (int) (mLayoutParams.y - yDelta);
                        mLayoutParams.height = (int) (mLayoutParams.height + yDelta);
                        break;
                    case ACTION_BOTTOM_DRAG:
                        if (mLayoutParams.height - yDelta <= mClipper.getMinHeight()) {
                            mLayoutParams.height = mClipper.getMinHeight();
                            break;
                        }
                        if (mLayoutParams.height - yDelta >= mClipper.getMaxHeight()) {
                            mLayoutParams.height = mClipper.getMaxHeight();
                            break;
                        }
                        mLayoutParams.height = (int) (mLayoutParams.height - yDelta);
                        break;
                    case ACTION_CENTER_DRAG:
                        if (mLayoutParams.y - yDelta <= 0) {
                            mLayoutParams.y = 0;
                            break;
                        }
                        if (mLayoutParams.y + mLayoutParams.height - yDelta >= SCREEN_H) {
                            mLayoutParams.y = SCREEN_H - mLayoutParams.height;
                            break;
                        }
                        mLayoutParams.y = (int) (mLayoutParams.y - yDelta);
                        break;
                    default:
                        break;
                }
                mDragDownY = rawY;
                mClipper.getRectF().bottom = mLayoutParams.height - mClipper.getDragBitmapOffset();
                mWm.updateViewLayout(mView, mLayoutParams);
                //mClipper.invalidate();
                break;
            case MotionEvent.ACTION_UP:
                mClipper.setDragAction(-1);
                Log.d(TAG, "onTouch: " + "x = " + mLayoutParams.x
                    + ", y = " + mLayoutParams.y
                    + ", height = " + mLayoutParams.height
                    + ", width = " + mLayoutParams.width);
                break;
        }
        return true;
    }

    public int getX() {
        return mLayoutParams.x;
    }

    public int getY() {
        return mLayoutParams.y;
    }

    public int getHeight() {
        return mLayoutParams.height - mClipper.getDragBitmapOffset();
    }

    public int getWidth() {
        return mLayoutParams.width;
    }
}
