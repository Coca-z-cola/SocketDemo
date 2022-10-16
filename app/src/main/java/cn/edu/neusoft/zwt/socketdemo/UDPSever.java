package cn.edu.neusoft.zwt.socketdemo;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class UDPSever {

    private static UDPSever udpServer = new UDPSever();
    private static final String TAG = "UDPServer";

    private DatagramSocket socket = null;
    private static final String IP = "255.255.255.255"; //发送给整个局域网
    private int port = 8888;  //发送方和接收方需要端口一致
    private int waitTime = 10000;
    //  private static String[] commands = new String[]{"IP", "MAC", "TIME","IP_MAC"};
    //private static String[] commands = new String[]{"IP_MAC"};
    //可以理解为
    private boolean running = false;

    //public static final String IP_MAC = "IP_MAC";
    public static final String SHUTDOWN = "SHUTDOWN";
    public static final String REBOOT = "REBOOT";
    public static final String EXIT = "EXIT";
    public static final String LedOn="LedOn";
    public static final String PLAY="PLAY";
    public static final String LedOff="LedOff";
    public static final String FansOn ="FansOn";
    public static final String FansOff="FansOff";
    public static final String BeepOn="BeepOn";
    public static final String BeepOff="BeepOff";


    private UDPSever() {

    }

    public static UDPSever getInstance() {
        return udpServer;
    }
    //实例化
    public void initData(int port, int waitTime) {
        this.port = port;
        this.waitTime = waitTime;
    }

    public void sendSearchGateWay(final Handler myHandler) {
        sendSearchGateWay(IP, myHandler);
    }

    public void sendSearchGateWay(String ipAddress, final Handler myHandler) {
        sendSearchGateWay(ipAddress, new String[]{"IP_MAC"}, myHandler);
    }

    public void sendSearchGateWay(String ipAddress, final String command, final Handler myHandler) {
        Log.d("TMP", "command:" +command);
        sendSearchGateWay(ipAddress, new String[]{command}, myHandler);
    }
    //判断是否输入ip地址
    public void sendSearchGateWay(String ipAddress, final String[] commands, final Handler myHandler) {
        //初始化socket
        if (running) {
            sendMagViaHandle(myHandler, "服务正在运行", MainActivity.GETNEWDATA);
            return;
        }
        if (TextUtils.isEmpty(ipAddress)) {
            sendMagViaHandle(myHandler, "请输入IP地址", MainActivity.GETNEWDATA);
            return;
        }
        try {
            socket = new DatagramSocket();
            //设置超时时间
            socket.setSoTimeout(waitTime);
        } catch (SocketException e) {
            e.printStackTrace();
            if (e.getMessage() != null) {
                sendMagViaHandle(myHandler, e.getMessage().toString(), MainActivity.GETNEWDATA);
            } else {
                sendMagViaHandle(myHandler, "Socket 初始化错误", MainActivity.GETNEWDATA);
            }
            return;
        }
        InetAddress mAddress;
        try {
            mAddress = InetAddress.getByName(ipAddress);
            //通过ip，获取ip地址
        } catch (UnknownHostException e) {
            e.printStackTrace();
            if (e.getMessage() != null) {
                sendMagViaHandle(myHandler, e.getMessage().toString(), MainActivity.GETNEWDATA);
            } else {
                sendMagViaHandle(myHandler, "地址初始化错误", MainActivity.GETNEWDATA);
            }
            return;
        } catch (Exception e) {
            return;
        }
        callSendThread(commands, myHandler,mAddress);
    }

    private void callSendThread(final String[] commands, final Handler myHandler, final InetAddress mAddress){
        //创建线程发送信息
        new Thread() {
            private byte[] sendBuf;

            @Override
            public void run() {
                try {
                    sendMsg(commands, myHandler,mAddress);
                } catch (SocketTimeoutException e) {
                    Log.d(TAG, "SocketTimeoutException");
                    e.printStackTrace();
                } catch (Exception e) {
                    Log.d(TAG, "Exception");
                    e.printStackTrace();
                    if (e.getMessage() != null) {
                        sendMagViaHandle(myHandler, e.getMessage().toString(), MainActivity.GETNEWDATA);
                    } else {
                        sendMagViaHandle(myHandler, "发送错误", MainActivity.GETNEWDATA);
                    }
                } finally {
                    Log.d(TAG, "Exit");
                    socket.close();
                    sendMagViaHandle(myHandler, "Exit", MainActivity.GETNEWDATA);
                }

            }
        }.start();
    }

    private void sendMsg(String command,InetAddress mAddress) throws Exception {
        byte[] sendBuf = command.getBytes("utf-8");

        DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, mAddress, port);

        //已发送完成
        socket.send(packet);
        Log.d(TAG, "Send MSG:" + command);
        byte[] receive = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receive, receive.length);

        socket.receive(receivePacket);
        String result = new String(receivePacket.getData(), 0, receivePacket.getLength(), "utf-8");
        Log.d(TAG, "result: " + result);

    }

    private void sendMsg(String[] command,Handler myHandler, InetAddress mAddress) throws Exception {
        for (int i = 0; i < command.length; i++) {
            byte[] sendBuf = getBytes(command[i]);
            DatagramPacket packet = getDatagramPacket(sendBuf, mAddress);
            socket.send(packet);//将数据发送到连接的套接字中
            Log.d(TAG, "Send MSG:" + command[i]);
        }

        byte[] receive = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receive, receive.length);
        while (true) {
            socket.receive(receivePacket);//数据从网络到达主机，读取数据
            String result = new String(receivePacket.getData(), 0, receivePacket.getLength(), "utf-8");

            //最后，调用DatagramSocket的receive()方法。直到数据包接收到为止，这个方法都是阻塞的。
            //接收的数据位于DatagramPacket的字节缓冲块。缓冲块可以通过调用getData()获得：

            Log.d(TAG, "result: " + result);
            sendMagViaHandle(myHandler, result, MainActivity.GETNEWDATA);
        }
    }

    private void sendMagViaHandle(Handler myHandler, String txt, int what) {
        Message msg = myHandler.obtainMessage();
        //Handler.obtainMessage()从一个可回收对象池中获取Message对象,避免重复Message创建对象；创建消息。
        msg.what = what;
        msg.obj = txt;
        myHandler.sendMessage(msg);
        //发送消息
        Log.d("TAG","sendMagViaHandle:" + txt);
    }

    private byte[] getBytes(String str) throws Exception {
        return str.getBytes("utf-8");
    }

    private DatagramPacket getDatagramPacket(byte[] sendBuf, InetAddress mAddress) {
        return new DatagramPacket(sendBuf, sendBuf.length, mAddress, port);
    }//将length长的buf数据发送到指定的地址的端口号处
}
