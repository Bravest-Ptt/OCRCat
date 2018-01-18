package bravest.ptt.ocrcat.ocr.baidu;

import android.util.Log;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.GeneralBasicParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.WordSimple;

import java.io.File;

import bravest.ptt.ocrcat.App;
import bravest.ptt.ocrcat.ocr.OcrAbstract;

/**
 * Created by pengtian on 2018/1/17.
 */

public class BaiduOcr extends OcrAbstract {

    private static final String TAG = "BaiduOcr";
    
    private static final String AK = "NgKwdnkBVL2e6cn1KVl6zfPA";
    private static final String SK = "HWqI5URLGct4dCZABKFp1t4MyNChjuzV";

    public static void init() {
        OCR.getInstance().initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                // 调用成功，返回AccessToken对象
                String token = result.getAccessToken();
                Log.d(TAG, "onOcrResult: token = " + token);
            }
            @Override
            public void onError(OCRError error) {
                // 调用失败，返回OCRError子类SDKError对象
                Log.e(TAG, "onOcrError: error code = " + error.getErrorCode());
            }
        }, App.Context(), AK, SK);
    }

    @Override
    public void recognize(String imagePath) {
        GeneralBasicParams param = new GeneralBasicParams();
        param.setDetectDirection(true);
        param.setImageFile(new File(imagePath));

        // 调用通用文字识别服务
        OCR.getInstance().recognizeGeneralBasic(param, new OnResultListener<GeneralResult>() {
            @Override
            public void onResult(GeneralResult result) {
                // 调用成功，返回GeneralResult对象
                StringBuffer r = new StringBuffer();
                if (result.getWordList().size() > 1) {
                    Log.d(TAG, "onResult: size  > 1");
                }
                for (WordSimple wordSimple : result.getWordList()) {
                    // wordSimple不包含位置信息
                    r.append(wordSimple.getWords());
                }
                Log.d(TAG, "onResult: r = " + r);
                if (mOcrResultListener != null) {
                    //mOcrResultListener.onOcrResult(result.getJsonRes());
                    mOcrResultListener.onOcrResult(r.toString());
                }
            }
            @Override
            public void onError(OCRError error) {
                // 调用失败，返回OCRError对象
                if (mOcrResultListener != null)
                    mOcrResultListener.onOcrError("百度：" + error.getErrorCode() + "");
            }
        });
    }
}
