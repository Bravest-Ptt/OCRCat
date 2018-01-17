package bravest.ptt.ocrcat.ocr;

import java.io.File;

/**
 * Created by pengtian on 2018/1/17.
 */

public interface IOcr {
    void recognize(String imagePath);
    void setResultListener(OcrResultListener listener);

    public interface OcrResultListener {
        void onResult(String result);
        void onError(String error);
    }
}
