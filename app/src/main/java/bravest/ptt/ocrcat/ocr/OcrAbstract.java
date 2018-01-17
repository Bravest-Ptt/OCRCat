package bravest.ptt.ocrcat.ocr;

import android.content.Context;

import bravest.ptt.ocrcat.ocr.IOcr;

/**
 * Created by pengtian on 2018/1/17.
 */

public abstract class OcrAbstract implements IOcr {
    protected OcrResultListener mOcrResultListener;

    @Override
    public void setResultListener(OcrResultListener listener) {
        mOcrResultListener = listener;
    }
}
