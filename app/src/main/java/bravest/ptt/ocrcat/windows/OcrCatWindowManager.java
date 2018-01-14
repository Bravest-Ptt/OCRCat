package bravest.ptt.ocrcat.windows;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.Log;
import android.view.View;

import bravest.ptt.ocrcat.utils.ToastUtils;

/**
 * Created by pengtian on 2018/1/14.
 */

public class OcrCatWindowManager implements ScreenShotButton.OnScreenShotListener{
    private static final String TAG = "OcrCatWindowManager";

    private Context mContext;
    private ScreenShotButton mButton;
    private ScreenClipperWindow mClipper;
    private ResultWindow mResultWindow;

    public OcrCatWindowManager(Context context) {
        mContext = context;
        initWindows();
    }

    private void initWindows() {
        mButton = new ScreenShotButton(mContext);
        mClipper = new ScreenClipperWindow(mContext);
        mResultWindow = new ResultWindow(mContext);

        mButton.setScreenShotListener(this);
    }

    public void showWindows(boolean show) {
        if (show) {
            mButton.show();
            mClipper.show();
            mResultWindow.show();
        } else {
            mButton.hide();
            mClipper.hide();
            mResultWindow.hide();
        }
    }

    public boolean isWindowsShowing() {
        return mButton.isShowing() || mClipper.isShowing() || mResultWindow.isShowing();
    }

    @Override
    public void onScreenShot(Bitmap bitmap) {
        Log.d(TAG, "onScreenShot: bitmap = " + bitmap);
    }

    public void setUpMediaProjection(MediaProjection mp) {
        mButton.setUpMediaProjection(mp);
    }
}
