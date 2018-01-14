package bravest.ptt.ocrcat.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import bravest.ptt.ocrcat.MainActivity;
import bravest.ptt.ocrcat.R;
import bravest.ptt.ocrcat.windows.OcrCatWindowManager;

/**
 * Created by pengtian on 2018/1/14.
 */

/**
 *  前台服务， 为悬浮窗提供上下文并和主Activity进行通信，提供操作接口
 */
public class OcrCatService extends Service{

    private static final String TAG = "OcrCatService";

    private static final int FOREGROUND_SERVICE_NOTIFICATION_ID = 528;

    private OcrCatWindowManager mOcrCatWindowManager;
    private OcrCatBinder mBinder = new OcrCatBinder();

    public class OcrCatBinder extends Binder {
        public OcrCatService getService() {
            return OcrCatService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mOcrCatWindowManager = new OcrCatWindowManager(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        builder.setContentIntent(contentIntent);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setTicker("Foreground Service Start");
        builder.setContentTitle("Foreground Service");
        builder.setContentText("Make this service run in the foreground.");
        builder.setPriority(1000);
        builder.setAutoCancel(false);

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;

        startForeground(FOREGROUND_SERVICE_NOTIFICATION_ID, notification);
        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void showWindows(boolean show) {
        mOcrCatWindowManager.showWindows(show);
    }

    public boolean isWindowsShowing() {
        return mOcrCatWindowManager.isWindowsShowing();
    }

    public void setUpMediaProjection(MediaProjection mp) {
        mOcrCatWindowManager.setUpMediaProjection(mp);
    }
}
