package bravest.ptt.ocrcat.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

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
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        Notification notification;
        if (Build.VERSION.SDK_INT >= 26) {
            String channelId = createNotificationChannel();
            notification = new Notification.Builder(this, channelId)
                    .setContentIntent(contentIntent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker("Foreground Service Start")
                    .setContentTitle("Foreground Service")
                    .setContentText("Make this service run in the foreground.")
                    .setAutoCancel(false)
                    .build();
        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setContentIntent(contentIntent);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setTicker("Foreground Service Start");
            builder.setContentTitle("Foreground Service");
            builder.setContentText("Make this service run in the foreground.");
            builder.setPriority(1000);
            builder.setAutoCancel(false);
            notification = builder.build();
        }

        notification.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(FOREGROUND_SERVICE_NOTIFICATION_ID, notification);
        return START_STICKY;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel() {
        String channelId = "Ocrcat";
        String channelName = "Ocrcat Background Service";
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_HIGH);
        chan.setLightColor(Color.BLUE);
        chan.setImportance(NotificationManager.IMPORTANCE_NONE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service
                = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
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
