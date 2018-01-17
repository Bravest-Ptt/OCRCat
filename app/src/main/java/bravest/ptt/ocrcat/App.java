package bravest.ptt.ocrcat;

import android.app.Application;
import android.content.Context;

import bravest.ptt.ocrcat.ocr.IOcr;
import bravest.ptt.ocrcat.ocr.baidu.BaiduOcr;

/**
 * Created by pengtian on 2018/1/17.
 */

public class App extends Application {
    private static Context sContext =  null;

    @Override
    public void onCreate() {
        super.onCreate();
        if (sContext == null) {
            sContext = getApplicationContext();
        }

        initOcr();
    }

    private void initOcr() {
        BaiduOcr.init();
    }

    public static Context Context() {
        return sContext;
    }
}
