package bravest.ptt.ocrcat.windows.i;

import android.view.View;

/**
 * Created by pengtian on 2018/1/14.
 */

public abstract class AbstractWindow implements IWindow {
    protected boolean mIsShowing = false;
    protected View mView;

    @Override
    public boolean isShowing() {
        return mIsShowing;
    }
}
