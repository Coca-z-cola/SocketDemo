package cn.edu.neusoft.zwt.socketdemo;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Base64;



//语音识别

public class MainActivity extends AppCompatActivity {

    public static final int GETNEWDATA = 2020022700;
    private MyHandler myHandler;
    private MyHandlerCommand myHandlerCommand;
    private TextView tv,  tv_command_result;
    private EditText et_ip;
    private ImageButton bt, bt_reboot, bt_play, bt_shutdown_server,bt_shutdown;
    private Button bt_LedOn,bt_LedOff,bt_FansOn,bt_FansOff,bt_DoorOpen,bt_DoorClose,bt_BeepOn,bt_BeepOff;
    //private VideoView video;
    private ImageView video;
    Bitmap bp;
    private DatagramSocket receiveSocket;
    private DatagramSocket reveSocket;
    private InetAddress serverAddr;
    private final static int RECEIVE_PORT = 9999;
    //private MediaController mediaController;

    //语音识别
    TextView tv_Speech;
    TextView tv_Result;
    FloatingActionButton actionButton;
    VoiceServiceManager serviceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //隐藏状态栏
        tv = findViewById(R.id.tv);
        et_ip = findViewById(R.id.et_ip);
        tv_command_result = findViewById(R.id.tv_command_result);

        if(getRequestedOrientation()!=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }

        myHandler = new MyHandler(this);
        myHandlerCommand = new MyHandlerCommand(this);
        bt_reboot = findViewById(R.id.bt_reboot);
        bt_reboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bt.setVisibility(View.VISIBLE);//重启后，搜索按钮可见
                tv_command_result.setText("");
                UDPSever.getInstance().sendSearchGateWay(getAddress(),
                        UDPSever.REBOOT, myHandlerCommand);
            }
        });

        bt_play = findViewById(R.id.bt_play);
        bt_play.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                //bt.setVisibility(View.INVISIBLE);//————————————————————连接视频后，搜索按钮不可见
                video = (ImageView) findViewById(R.id.vv);
                UdpReceiveThread thread= new UdpReceiveThread();
                thread.start();
                tv_command_result.setText("");
                UDPSever.getInstance().sendSearchGateWay(getAddress(),
                        UDPSever.PLAY, myHandlerCommand);
            }
        });

        bt_shutdown_server = findViewById(R.id.bt_shutdown_server);
        bt_shutdown_server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_command_result.setText("");
                UDPSever.getInstance().sendSearchGateWay(getAddress(),
                        UDPSever.EXIT, myHandlerCommand);
            }
        });
        bt_shutdown = findViewById(R.id.bt_shutdown);
        bt_shutdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_command_result.setText("");
                UDPSever.getInstance().sendSearchGateWay(getAddress(),
                        UDPSever.SHUTDOWN, myHandlerCommand);
            }
        });
        bt = findViewById(R.id.bt_find);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bt.setEnabled(false);
                // bt.setText("搜索中。。。。。");
                tv.setText("");
                UDPSever.getInstance().sendSearchGateWay(myHandler);
                //SendAndReceiveUtils.sendSearchGateWay(myHandler);

            }
        });
        ///////////////////////////////////////////////////////
        bt_LedOn = findViewById(R.id.bt_LedOn);
        bt_LedOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_command_result.setText("");
                UDPSever.getInstance().sendSearchGateWay(getAddress(),
                        UDPSever.LedOn, myHandlerCommand);
            }
        });
        bt_LedOff = findViewById(R.id.bt_LedOff);
        bt_LedOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_command_result.setText("");
                UDPSever.getInstance().sendSearchGateWay(getAddress(),
                        UDPSever.LedOff, myHandlerCommand);
            }
        });
        bt_FansOn = findViewById(R.id.bt_FansOn);
        bt_FansOn .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_command_result.setText("");
                UDPSever.getInstance().sendSearchGateWay(getAddress(),
                        UDPSever.FansOn, myHandlerCommand);
            }
        });
        bt_FansOff = findViewById(R.id.bt_FansOff);
        bt_FansOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_command_result.setText("");
                UDPSever.getInstance().sendSearchGateWay(getAddress(),
                        UDPSever.FansOff, myHandlerCommand);
            }
        });
//        bt_DoorOpen = findViewById(R.id.bt_DoorOpen);
//        bt_DoorOpen.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                tv_command_result.setText("");
//                UDPSever.getInstance().sendSearchGateWay(getAddress(),
//                        UDPSever.DoorOpen, myHandlerCommand);
//            }
//        });
//        bt_DoorClose = findViewById(R.id.bt_DoorClose);
//        bt_DoorClose.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                tv_command_result.setText("");
//                UDPSever.getInstance().sendSearchGateWay(getAddress(),
//                        UDPSever.DoorClose, myHandlerCommand);
//            }
//        });
        bt_BeepOn = findViewById(R.id.bt_BeepOn);
        bt_BeepOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_command_result.setText("");
                UDPSever.getInstance().sendSearchGateWay(getAddress(),
                        UDPSever.BeepOn,myHandlerCommand);
            }
        });
        bt_BeepOff = findViewById(R.id.bt_BeepOff);
        bt_BeepOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_command_result.setText("");
                UDPSever.getInstance().sendSearchGateWay(getAddress(),
                        UDPSever.BeepOff,myHandlerCommand);
            }
        });

        //语音识别
        tv_Speech = findViewById(R.id.tv_speech);
        tv_Result = findViewById(R.id.tv_Result);
        actionButton = findViewById(R.id.floatingActionButton);
        // swWakeUp = findViewById(R.id.switchWakeUp);

        initPermission();//动态权限

        serviceManager = new VoiceServiceManager(this);
        serviceManager.setVoiceListener(voiceListener);
        serviceManager.setOnBindedListener(new VoiceServiceManager.OnBindedListener() {
            @Override
            public void onBinded() {
                //  swWakeUp.setChecked(serviceManager.wakeupIsStart());
            }
        });
        serviceManager.start();

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceManager.asrStart();

            }
        });
    }

    //语音识别2
    @Override
    protected void onStart() {
        super.onStart();
        serviceManager.bind();
    }

    @Override
    protected void onStop() {
        super.onStop();
        serviceManager.unbind();
    }

     VoiceListener voiceListener = new VoiceListener.Stub() {
        @Override
        public void onEvent(int e, String value) throws RemoteException {
            switch (e) {
                case EVENT_ASR_READY:
                    break;
                case EVENT_ASR_PARTIAL_RESULT:
                    tv_Result.setText(value);
                    break;
                case EVENT_ASR_FINAL_RESULT:
                    //tv_Speech.setText("\n");
                    tv_Speech.setText(value);
                    tv_command_result.setText("");
                    UDPSever.getInstance().sendSearchGateWay(getAddress(),
                           value,myHandlerCommand);
                    tv_Result.setText(value);

                    break;
                case EVENT_ASR_FINISH:
                    tv_Result.setText("");
                    break;

                case EVENT_WAKEUP_SUCCESS:
                    //tv_Speech.setText(value);
                    break;
                default:
                    break;
            }
        }
    };
    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String[] permissions = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };

        ArrayList<String> toApplyList = new ArrayList<>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.

            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }



    //创建线程
    public class UdpReceiveThread extends Thread
    {
        @Override
        public void run()
        {
            try
            {
                reveSocket = new DatagramSocket(RECEIVE_PORT);
                //创建数据包套接字绑定到指定端口
                serverAddr = InetAddress.getByName(getAddress());
                //在给定主机名的情况下确定主机的IP地址
                // ByteArrayOutputStream out = new ByteArrayOutputStream();
                while(true)
                {
                    byte[] inBuf= new byte[1024*1024];
                    DatagramPacket inPacket=new DatagramPacket(inBuf,inBuf.length);
                    //接受数据包
                    //out.write(inPacket.getData());
                    reveSocket.receive(inPacket);
                    if(!inPacket.getAddress().equals(serverAddr)){
                        throw new IOException("未知名的报文");
                    }

                    ByteArrayInputStream in = new ByteArrayInputStream(inPacket.getData());
                    //字节数据输入流
                    bp = BitmapFactory.decodeStream(in);
                    //数据转化为图片
                    video.setImageBitmap(bp);
                }


            } catch (Exception e)
            {
                e.printStackTrace();
            }finally {
                reveSocket.close();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static byte[] getImgBase64ToImgFile(String imgBase64) {
        boolean flag = true;
        byte[] bytes=null;
        try {
            // 解密处理数据
            //byte[] bytes = new BASE64Decoder().decodeBuffer(imgBase64);
            Base64.Decoder decoder = Base64.getDecoder();
            bytes = decoder.decode(imgBase64);
            for (int i = 0; i < bytes.length; ++i) {
                if (bytes[i] < 0) {
                    bytes[i] += 256;
                }
            }
            //outputStream = new FileOutputStream(imgPath);
            //outputStream.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            flag = false;
        }
        return bytes;
    }

    @Override
    protected void onResume() {
        super.onResume();
//        et_ip.setFocusable(false);
//        bt.setFocusable(true);
    }


    private String getAddress() {
        String ip = et_ip.getText().toString();//获取输入的ip地址值
        if (ip != null) {
            ip = ip.trim();//去掉字符串的一些空白
        }
        if (TextUtils.isEmpty(ip)) {
            Toast ts = Toast.makeText(getBaseContext(),"请输入树莓派IP地址",Toast.LENGTH_LONG);
            ts.show();
            return null;
        }
        if (!isIPAddressByRegex(ip)){
            Toast ts = Toast.makeText(getBaseContext(),"请输入正确的IP地址",Toast.LENGTH_LONG);
            ts.show();
            return null;
        }
        Log.d("TMP", "Server IP:" + ip);
        return ip;
    }

    public boolean isIPAddressByRegex(String str) {
        String regex = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
        // 判断ip地址是否与正则表达式匹配
        if (str.matches(regex)) {
            String[] arr = str.split("\\.");
            for (int i = 0; i < 4; i++) {
                int temp = Integer.parseInt(arr[i]);
                //如果某个数字不是0到255之间的数 就返回false
                if (temp < 0 || temp > 255){
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public static class MyHandler extends Handler {

        private WeakReference<MainActivity> activityWeakReference;


        public MyHandler(MainActivity activity) {
            activityWeakReference = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);//调用父类方法
            MainActivity activity = activityWeakReference.get();
            if (activity != null) {
                int what = msg.what;
                switch (what) {
                    case GETNEWDATA:
                        String tmp = (String) msg.obj;
                        if (tmp.equals("Exit")) {
                            activity.bt.setEnabled(true);
                            //   activity.bt.setText("搜索");
                        }
                        activity.tv.append("\n" + tmp);
                        break;
                    default:
                        break;
                }
            }
        }
    }


    public static class MyHandlerCommand extends Handler {

        private WeakReference<MainActivity> activityWeakReference;

        public MyHandlerCommand(MainActivity activity) {
            activityWeakReference = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = activityWeakReference.get();
            if (activity != null) {
                int what = msg.what;
                switch (what) {
                    case GETNEWDATA:
                        String tmp = (String) msg.obj;
                        activity.tv_command_result.append("\n" + tmp);
                        break;
                    default:
                        break;
                }
            }
        }
    }
}