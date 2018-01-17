package bravest.ptt.ocrcat.network;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by pengtian on 2018/1/17.
 */

public class UdpSendThread implements Runnable {

    private static final String TAG = "UdpSendThread";

    private DatagramSocket mSocket = null;
    private String mServerAddress;
    private int mServerPort;
    private int mPort;
    private String mMessage;

    public UdpSendThread(String sa, int sp, int p) {
        if (TextUtils.isEmpty(sa) || sp == 0 || p == 0) {
            throw new IllegalArgumentException("arg error");
        }
        mServerAddress = sa;
        mServerPort = sp;
        mPort = p;
    }

    @Override
    public void run() {
        if (mSocket == null) {
            Log.e(TAG, "run: socket is null");
            return;
        }
        Log.d(TAG, "run: Message = " + mMessage);
        Log.d(TAG, "run: server address = " + mServerAddress);
        Log.d(TAG, "run: server port = " + mServerPort);
        Log.d(TAG, "run: client port = " + mPort);
        try {
            InetAddress serverAddress = InetAddress.getByName(mServerAddress);
            byte data[] = mMessage.getBytes();//把字符串str字符串转换为字节数组
            //创建一个DatagramPacket对象，用于发送数据。
            //参数一：要发送的数据  参数二：数据的长度  参数三：服务端的网络地址  参数四：服务器端端口号
            DatagramPacket packet = new DatagramPacket(data, data.length ,serverAddress ,mServerPort);
            mSocket.send(packet);//把数据发送到服务端。
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "run: io error " + e.getMessage());
        }finally {
            mSocket.close();
        }
    }

    public void send(String message) {
        if (message == null) {
            Log.e(TAG, "send: message is null");
            return;
        }

        mMessage = message;

        try {
            mSocket = new DatagramSocket(mPort);
        } catch (SocketException e) {
            Log.e(TAG, "UdpSendThread: init error " + e.getMessage());
            e.printStackTrace();
        }

        if (mSocket == null) {
            Log.e(TAG, "send: socket is null");
            return;
        }

        new Thread(this).start();
    }

    public void close() {
        if (mSocket != null) {
            mSocket.close();
        }
    }
}
