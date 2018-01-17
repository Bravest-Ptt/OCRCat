package bravest.ptt.ocrcat.windows;

import android.content.Context;
import android.view.View;

import bravest.ptt.ocrcat.windows.i.AbstractWindow;

/**
 * Created by pengtian on 2018/1/14.
 */

public class ResultWindow extends AbstractWindow{
    private static final String TAG = "ResultWindow";

    private Context mContext;

    public ResultWindow(Context context) {
        mContext = context;
    }

    @Override
    public void initVariables() {

    }

    @Override
    public void initView() {

    }

    @Override
    public void initLayoutParams() {

    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void destroy() {

    }

    public void setVisible(boolean visible) {
        if (mView != null) {
            mView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }
}
