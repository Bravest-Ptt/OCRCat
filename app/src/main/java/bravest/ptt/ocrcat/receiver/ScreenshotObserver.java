package bravest.ptt.ocrcat.receiver;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

/**
 * Created by pengtian on 2018/1/12.
 */

public class ScreenshotObserver extends ContentObserver {

    private static final String TAG = "ScreenshotObserver";

    private Context mContext;
    private Handler mHandler;
    public ScreenshotObserver(Context context, Handler handler) {
        super(handler);
        mContext = context;
        mHandler = handler;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Log.d(TAG, "-----------------database is changed ------------------------");
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, "datetaken desc");

        if (cursor != null) {
            Log.d(TAG, "The number of cursor is " + cursor.getCount());
            StringBuffer sb = new StringBuffer();
            if (cursor.moveToNext()) {
                String fileName = cursor.getString(cursor.getColumnIndex("_data"));
                Log.d(TAG, fileName);
            }
        }
    }
}
