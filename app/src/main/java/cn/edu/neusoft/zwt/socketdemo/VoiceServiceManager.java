package cn.edu.neusoft.zwt.socketdemo;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.os.Build.VERSION.SDK_INT;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import cn.edu.neusoft.zwt.socketdemo.VoiceListener;
import cn.edu.neusoft.zwt.socketdemo.VoiceBinder;
public class VoiceServiceManager extends VoiceBinder.Stub {
    private static final String TAG = "VoiceServiceManager";
    public static final String SERVICE_NAME = "cn.edu.neusoft.zwt.socketdemo.VoiceService";
    public static final String SERVICE_PACKEG = "cn.edu.neusoft.zwt.socketdemo";
    private Context context;
    private VoiceBinder binder;
    private VoiceListener listener;
    private OnBindedListener onBindedListener;
    private boolean isBinded = false;
    private static VoiceServiceManager instance;

    public VoiceServiceManager(Context context) {
        this.context = context;
    }

    public static VoiceServiceManager getInstance(Context context) {
        if (instance == null) {
            instance = new VoiceServiceManager(context);
        }
        return instance;
    }
    public void start() {
        //如果服务没有启动，则启动服务
        if (!cn.edu.neusoft.zwt.socketdemo.ServiceUtil.isServiceRunning(context, SERVICE_NAME)) {
            Intent i = new Intent();
            i.setComponent(new ComponentName(SERVICE_PACKEG, SERVICE_NAME));
            if (SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(i);
            }else {
                context.startService(i);
            }
        }
    }

    public void stop() {
        //如果服务没有启动，则启动服务
        if (cn.edu.neusoft.zwt.socketdemo.ServiceUtil.isServiceRunning(context, SERVICE_NAME)) {
            Intent i = new Intent();
            i.setComponent(new ComponentName(SERVICE_PACKEG, SERVICE_NAME));
            context.stopService(i);
        }
    }

    public boolean isStared() {
        return cn.edu.neusoft.zwt.socketdemo.ServiceUtil.isServiceRunning(context, SERVICE_NAME);
    }

    public void setOnBindedListener(OnBindedListener listener) {
        onBindedListener = listener;
    }

    public void setVoiceListener(VoiceListener listener) {
        this.listener = listener;
    }
    public void bind() {
        if(isBinded)return;
        if (!isStared()) {
            start();
        }
        //绑定服务
        Intent i = new Intent();
        i.setComponent(new ComponentName(SERVICE_PACKEG, SERVICE_NAME));
        context.bindService(i, connection, BIND_AUTO_CREATE);
    }

    public void unbind() {
        if(!isBinded) return;
        if (binder != null) {
            try {
                binder.unregisterListener(listener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        //解绑服务
        if (context != null) {
            context.unbindService(connection);
        }
        isBinded = false;
    }

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected: is called");
            isBinded = true;
            binder = VoiceBinder.Stub.asInterface(iBinder);
            if (binder != null) {
                try {
                    binder.registerListener(listener);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                if(onBindedListener !=null) onBindedListener.onBinded();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected: is called");
            binder = null;
            isBinded = false;
        }
    };

    interface OnBindedListener {
        void onBinded();
    }

    ///////////以下为接口原有方法的实现//////////////

    @Override
    public void asrStart() {
        if(binder!=null) {
            try {
                binder.asrStart();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void asrStop() {
        if(binder!=null) {
            try {
                binder.asrStop();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void asrCancel() {
        if(binder!=null) {
            try {
                binder.asrCancel();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void ttsSpeak(String text) {
        if(binder!=null) {
            try {
                binder.ttsSpeak(text);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void ttsStop() {
        if(binder!=null) {
            try {
                binder.ttsStop();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void wakeupStart() {
        if(binder!=null) {
            try {
                binder.wakeupStart();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void wakeupStop() {
        if(binder!=null) {
            try {
                binder.wakeupStop();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean wakeupIsStart() {
        if(binder!=null) {
            try {
                return binder.wakeupIsStart();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void registerListener(VoiceListener listener) {
        if(binder!=null) {
            try {
                binder.registerListener(listener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void unregisterListener(VoiceListener listener) {
        if(binder!=null) {
            try {
                binder.unregisterListener(listener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
