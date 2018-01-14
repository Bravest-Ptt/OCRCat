package bravest.ptt.ocrcat.windows;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import bravest.ptt.ocrcat.R;
import bravest.ptt.ocrcat.utils.DensityUtil;
import bravest.ptt.ocrcat.windows.i.AbstractWindow;

/**
 * Created by pengtian on 2018/1/14.
 */

public class ScreenClipperWindow extends AbstractWindow{
    private static final String TAG = "ScreenClipperWindow";

    private Context mContext;
    private WindowManager mWm;
    private WindowManager.LayoutParams mLayoutParams;
    private LayoutInflater mInflater;

    public ScreenClipperWindow(Context context) {
        mContext = context;
        initVariables();
        initView();
    }

    @Override
    public void initVariables() {
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
    }

    @Override
    public void initLayoutParams() {
        mLayoutParams.flags = mLayoutParams.flags
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.dimAmount = 0.2f;
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.gravity = Gravity.START | Gravity.TOP;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.alpha = 1.0f;
        int[] wh = DensityUtil.getDeviceInfo(mContext);
        mLayoutParams.x = 0;
        mLayoutParams.y = wh[1] - 10;
    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void destroy() {

    }

    public void getRect() {
        return;
    }
}
