package bravest.ptt.ocrcat.network;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by pengtian on 2018/1/17.
 */

public class UdpReceiveThread implements Runnable {

    private static final String TAG = "UdpReceiveThread";
    
    public static final int WHAT_RECEIVE = 1;

    private boolean mIsReceiving = false;
    private Handler mHandler = null;
    private DatagramSocket mSocket;
    private Thread mThread;

    public UdpReceiveThread(Handler handler, int port) {
        mHandler = handler;
        try {
            mSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            Log.e(TAG, "UdpReceiveThread: init error" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        int bufferLength = 2048;
        byte data[] = new byte[bufferLength];
        DatagramPacket packet = new DatagramPacket(data, data.length);

        if (mSocket == null) {
            Log.e(TAG, "run: mSocket == null");
            return;
        }
        Log.d(TAG, "run: mIsReceiving = " + mIsReceiving);

        while (mIsReceiving) {
            try {
                mSocket.receive(packet);
            } catch (IOException e) {
                Log.e(TAG, "run: UDP数据包接收失败！");
                e.printStackTrace();
                continue;
            }
            if (packet.getLength() == 0) {
                Log.e(TAG, "run: 接收到的UDP数据为空");
                continue;
            }
            String result = new String(packet.getData(), 0, packet.getLength());
            Log.d(TAG, result + " from " + packet.getAddress().getHostAddress() + ":" + packet.getPort());
            packet.setLength(bufferLength);

            Message msg = Message.obtain();
            msg.obj = result;
            msg.what = WHAT_RECEIVE;
            mHandler.sendMessage(msg);
        }
    }

    public void setRunning(boolean run) {
        mIsReceiving = run;
        if (run) {
            mThread = new Thread(this);
            mThread.start();
        } else {
            if (mThread != null) {
                mThread.interrupt();
                mThread = null;
            }
        }
    }

    public void close() {
        if (mSocket != null) {
            mSocket.close();
        }
    }
}
