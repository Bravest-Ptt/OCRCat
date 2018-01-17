package bravest.ptt.ocrcat.windows;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.projection.MediaProjection;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

import bravest.ptt.ocrcat.utils.DensityUtil;

/**
 * Created by pengtian on 2018/1/14.
 */

public class OcrCatWindowManager implements ScreenShotButton.OnScreenShotListener{
    private static final String TAG = "OcrCatWindowManager";

    private Context mContext;
    private ScreenShotButton mButton;
    private ScreenClipperWindow mClipper;
    private ResultWindow mResultWindow;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };

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

    public void setUpMediaProjection(MediaProjection mp) {
        mButton.setUpMediaProjection(mp);
    }

    @Override
    public void onScreenShotStart() {
        mClipper.setVisible(false);
        mResultWindow.setVisible(false);
    }

    @Override
    public void onScreenShotEnd(Bitmap bitmap) {
        mClipper.setVisible(true);
        mResultWindow.setVisible(true);

        if (bitmap == null) {
            Log.e(TAG, "onScreenShotEnd: " + "screen shot get bitmap error");
            return;
        }
        int x = mClipper.getX(); int y = mClipper.getY();
        int width = mClipper.getWidth();
        int height = mClipper.getHeight();
        Log.d(TAG, "onScreenShotEnd: b width = " + bitmap.getWidth()
                + ", b height = " + bitmap.getHeight());
        Bitmap b = Bitmap.createBitmap(bitmap, x, y + DensityUtil.getStatusBarHeightDp(mContext),
                width, height);
        if (b == null) {
            Log.e(TAG, "onScreenShotEnd: " + "create bitmap failed");
            return;
        }
        bitmap.recycle();

        String dirName = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/ocrcat";
        try {
            File dir = new File(dirName);
            if (!dir.exists()) {
                if (!dir.mkdirs()) Log.e(TAG, "onScreenShotEnd: " + "create dir failed");
            }
            File file = new File(dirName + "/bitmap" + ".jpg");
            FileOutputStream out = new FileOutputStream(file);
            b.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
