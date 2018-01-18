package bravest.ptt.ocrcat.ocr;

import java.io.File;

/**
 * Created by pengtian on 2018/1/17.
 */

public interface IOcr {
    void recognize(String imagePath);
    void setOcrResultListener(OcrResultListener listener);

    public interface OcrResultListener {
        void onOcrResult(String result);
        void onOcrError(String error);
    }
}
