package bravest.ptt.ocrcat.windows;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import java.nio.ByteBuffer;
import java.sql.BatchUpdateException;

import bravest.ptt.ocrcat.R;
import bravest.ptt.ocrcat.utils.DensityUtil;
import bravest.ptt.ocrcat.windows.i.AbstractWindow;

import static android.R.attr.width;

/**
 * Created by pengtian on 2018/1/14.
 * 一个在悬浮按钮，用来进行触发截屏，并得到截图的Bitmap
 */

public class ScreenShotButton extends AbstractWindow{

    private static final String TAG = "ScreenShotButton";

    private WindowManager mWm;
    private Context mContext;
    private WindowManager.LayoutParams mLayoutParams;
    private LayoutInflater mInflater;
    private OnScreenShotListener mScreenShotListener;
    private MediaProjection mMp;
    private ImageReader mImageReader;
    private VirtualDisplay mVirtualDisplay;

    private static final int WHAT_GET_BITMAP = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WHAT_GET_BITMAP:
                    releaseImageReader();
                    releaseVirtualDisplay();
                    if (mScreenShotListener != null) {
                        mScreenShotListener.onScreenShotEnd((Bitmap) msg.obj);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public interface OnScreenShotListener {
        void onScreenShotStart();
        void onScreenShotEnd(Bitmap bitmap);
    }

    public ScreenShotButton(Context context) {
        this.mContext = context;
        initVariables();
        initView();
        initLayoutParams();
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
        mView = mInflater.inflate(R.layout.layout_window_button, null);
        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screenShot();
            }
        });
    }

    /**
     * 核心方法，通过MediaProjectionManager获取的截图信息Bitmap
     */
    private void screenShot() {
        if (mScreenShotListener != null) mScreenShotListener.onScreenShotStart();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setUpVirtualDisplay();
            }
        }, 0);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startCapture();
            }
        }, 150);
    }

    private void startCapture() {
        Image image = mImageReader.acquireNextImage();
        if (image == null) {
            Log.e(TAG, "image is null.");
            return;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        Log.d(TAG, "startCapture: width = " + width
                + ", height = " + height);
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride,
                height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        image.close();

        Message msg = Message.obtain();
        msg.what = WHAT_GET_BITMAP;
        msg.obj = bitmap;
        mHandler.sendMessage(msg);
    }

    private void setUpVirtualDisplay() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        int width = DensityUtil.getDeviceWidth(mContext);
        int height = DensityUtil.getDeviceHeight(mContext);
        int dpi = DensityUtil.getDeviceDpi(mContext);
        Log.d(TAG, "setUpVirtualDisplay: dpi = " + dpi);
        mImageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1);
        mVirtualDisplay = mMp.createVirtualDisplay("ScreenShot", width, height, dpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(),
                null,null);
    }

    private void releaseImageReader() {
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    private void releaseVirtualDisplay() {
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
    }

    public void setScreenShotListener(OnScreenShotListener listener) {
        mScreenShotListener = listener;
    }

    public void setUpMediaProjection(MediaProjection mp) {
        mMp = mp;
    }

    @Override
    public void initLayoutParams() {
        mLayoutParams.flags = mLayoutParams.flags
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.dimAmount = 0.2f;
        mLayoutParams.type = Build.VERSION.SDK_INT < Build.VERSION_CODES.O ?
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY :
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.gravity = Gravity.START | Gravity.TOP;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.alpha = 1.0f;
        int[] wh = DensityUtil.getDeviceInfo(mContext);
        //mLayoutParams.x = wh[0];
        mLayoutParams.x = 0;
        mLayoutParams.y = wh[1] - 10;
        //onNormalPointChanged();
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
        //none
    }
}
