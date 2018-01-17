package bravest.ptt.ocrcat.network;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.NetworkOnMainThreadException;
import android.util.Log;

/**
 * Created by pengtian on 2018/1/17.
 */

public class UdpInterface {
    private static final String TAG = "UdpInterface";

    public static final String SERVER_ADDRESS = "192.168.43.187";
    public static final int SERVER_PORT = 50028;

    public static final int CLIENT_PORT_RECEIVE = 50028;
    public static final int CLIENT_PORT_SEND = 50029;

    private UdpSendThread mSender;
    private UdpReceiveThread mReceiver;
    private OnMessageObtainedListener mMessageListener;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            if (what == UdpReceiveThread.WHAT_RECEIVE && mMessageListener != null) {
                Log.d(TAG, "handleMessage: get message = " + msg.obj);
                mMessageListener.onMessageObtained(msg.obj.toString());
            }
        }
    };

    public interface OnMessageObtainedListener {
        void onMessageObtained(String msg);
    }

    public UdpInterface() {
        forceInMainThread();
        mReceiver = new UdpReceiveThread(mHandler, CLIENT_PORT_RECEIVE);
        mSender = new UdpSendThread(SERVER_ADDRESS, SERVER_PORT, CLIENT_PORT_SEND);
    }

    private void forceInMainThread() {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new NetworkOnMainThreadException();
        }
    }

    public void startReceiveThread() {
        mReceiver.setRunning(true);
    }

    public void stopReceiveThread() {
        mReceiver.setRunning(false);
    }

    public void close() {
        mReceiver.close();
        mSender.close();
    }

    public void send(String msg) {
        mSender.send(msg);
    }

    public void setMessageListener(OnMessageObtainedListener listener) {
        mMessageListener = listener;
    }
}
