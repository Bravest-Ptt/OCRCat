package bravest.ptt.ocrcat.windows;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import bravest.ptt.ocrcat.R;
import bravest.ptt.ocrcat.utils.DensityUtil;
import bravest.ptt.ocrcat.view.ClipBorderView;
import bravest.ptt.ocrcat.windows.i.AbstractWindow;

/**
 * Created by pengtian on 2018/1/14.
 */

public class ResultWindow extends AbstractWindow{
    private static final String TAG = "ResultWindow";

    private static int SCREEN_H  = 0;
    private static int SCREEN_W = 0;

    private Context mContext;
    private WindowManager mWm;
    private WindowManager.LayoutParams mLayoutParams;
    private LayoutInflater mInflater;
    private TextView mShower;

    public ResultWindow(Context context) {
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
        mView = mInflater.inflate(R.layout.layout_window_result, null);
        mShower = mView.findViewById(R.id.result_shower);
    }

    @Override
    public void initLayoutParams() {
        mLayoutParams.flags = mLayoutParams.flags
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE; //不接收事件
               // | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE; // 不接收焦点
        mLayoutParams.dimAmount = 0.2f;
        mLayoutParams.type = Build.VERSION.SDK_INT < Build.VERSION_CODES.O ?
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY :
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.width = SCREEN_W;
        mLayoutParams.gravity = Gravity.START | Gravity.TOP;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.alpha = 1.0f;
        mLayoutParams.x = 0;
        mLayoutParams.y = 0;
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
    }

    public void setVisible(boolean visible) {
        if (mView != null) {
            mView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public void setText(String text) {
        if (mShower != null) {
            mShower.setText(text);
        }
    }

    public void clear() {
        setText("");
    }
}
