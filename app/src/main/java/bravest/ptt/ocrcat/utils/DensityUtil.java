package bravest.ptt.ocrcat.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * liujingyuan
 */
public class DensityUtil {

    private static int[] deviceWidthHeight = new int[2];
    private static int deviceWidth = 0;
    private static int deviceHeight = 0;
    private static int deviceDpi = 0;

    public static int getDeviceWidth(Context context) {
        if (deviceWidth == 0) {
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            manager.getDefaultDisplay().getMetrics(metrics);
            deviceWidth =  metrics.widthPixels;
        }
        return deviceWidth;
    }

    public static int getDeviceHeight(Context context) {
        if (deviceHeight == 0) {
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            manager.getDefaultDisplay().getMetrics(metrics);
            deviceHeight =  metrics.heightPixels;
        }
        return deviceHeight;
    }

    public static int getDeviceDpi(Context context) {
        if (deviceDpi == 0) {
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            manager.getDefaultDisplay().getMetrics(metrics);
            deviceDpi =  metrics.densityDpi;
        }
        return deviceDpi;
    }

    public static int[] getDeviceInfo(Context context) {
        if ((deviceWidthHeight[0] == 0) && (deviceWidthHeight[1] == 0)) {
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            manager.getDefaultDisplay().getMetrics(metrics);

            deviceWidthHeight[0] = metrics.widthPixels;
            deviceWidthHeight[1] = metrics.heightPixels;
        }
        return deviceWidthHeight;
    }

    /**
     *
     * @param context 上下文
     * @param dpValue dp数值
     * @return dp to  px
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);

    }
    /**
     *
     * @param context    上下文
     * @param pxValue  px的数值
     * @return  px to dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int getStatusBarHeight(Context context) {
        if (context == null) {
            return 0;
        }
        int resourceId
                = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return (int) context.getResources().getDimension(resourceId);
    }

}
