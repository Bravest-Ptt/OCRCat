package bravest.ptt.ocrcat.windows.i;

/**
 * Created by pengtian on 2018/1/14.
 */

public interface IWindow {
    boolean isShowing();
    void initVariables();
    void initView();
    void initLayoutParams();
    void show();
    void hide();
    void destroy();
}
