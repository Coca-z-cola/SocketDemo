package cn.edu.neusoft.zwt.socketdemo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.baidu.aip.asrwakeup3.core.recog.MyRecognizer;
import com.baidu.aip.asrwakeup3.core.recog.RecogResult;
import com.baidu.aip.asrwakeup3.core.recog.listener.IRecogListener;
import com.baidu.aip.asrwakeup3.core.recog.listener.StatusRecogListener;
import com.baidu.speech.asr.SpeechConstant;

import java.util.LinkedHashMap;
import java.util.Map;
import cn.edu.neusoft.zwt.socketdemo.VoiceListener;
import cn.edu.neusoft.zwt.socketdemo.VoiceBinder;
public class VoiceService extends Service {
    private static final String TAG = "VoiceService";
    protected MyRecognizer mAsr;//语音识别对象

    private RemoteCallbackList<VoiceListener> mListenerList = new RemoteCallbackList<>();
    private boolean isContinueRecognize = false;

    public VoiceService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: is called");
        mAsr = new MyRecognizer(this, recogListener);//初始化asr
//        mTts = new NonBlockSyntherizer(this,getSpeechConfig(),null);// 初始化TTS引擎
//        mWakeup = new MyWakeup(this, wakeupListener);//初始化唤醒

        //回复保存的参数
        SharedPreferences shp = getSharedPreferences("voice_config", MODE_PRIVATE);
//        wakeupIsStarted = shp.getBoolean("wakeup_is_enabled", false);
//
//        //根据参数做设置
//        if(wakeupIsStarted) wakeupStart();

    }



    boolean wakeupIsStarted = false;
    @Override
    public IBinder onBind(Intent intent) {
        return new VoiceBinder.Stub() {

            @Override
            public void asrStart() throws RemoteException {
                VoiceService.this.asrStart();
            }

            @Override
            public void asrStop() throws RemoteException {
                mAsr.stop();
            }

            @Override
            public void asrCancel() throws RemoteException {
                mAsr.cancel();
            }

            @Override
            public void ttsSpeak(String text) throws RemoteException {
               // mTts.speak(text);
            }

            @Override
            public void ttsStop() throws RemoteException {
                //mTts.stop();
            }

            @Override
            public void wakeupStart() throws RemoteException {

            }

            @Override
            public void wakeupStop() throws RemoteException {

            }

            @Override
            public boolean wakeupIsStart() throws RemoteException {
                return wakeupIsStarted;
            }

            @Override
            public void registerListener(VoiceListener listener) throws RemoteException {
                if (listener == null) {
                    Log.e(TAG, "registerListener: listener is null");
                    return;
                }
                mListenerList.register(listener);
                Log.d(TAG, "registerListener: current size:" + mListenerList.getRegisteredCallbackCount());
            }

            @Override
            public void unregisterListener(VoiceListener listener) throws RemoteException {
                mListenerList.unregister(listener);
                Log.d(TAG, "unregisterListener: current size:" + mListenerList.getRegisteredCallbackCount());
            }
        };
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: is called.");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAsr.release();

        Log.i(TAG, "onDestory is called.");
    }


    private void broadcastEvent(int event, String value) {
        synchronized (mListenerList) {
            int n = mListenerList.beginBroadcast();
//            Log.d(TAG, "broadcastEvent: begin n="+n);
            try {
                for (int i = 0; i < n; i++) {
                    VoiceListener listener = mListenerList.getBroadcastItem(i);
                    if (listener != null) {
                        listener.onEvent(event,value);
                    }
                }
            } catch (RemoteException e) {
                Log.e(TAG, "broadcastEvent: error!");
                e.printStackTrace();
            }
            mListenerList.finishBroadcast();
//            Log.d(TAG, "broadcastEvent: end");
        }
    }


    /**
     * 语音识别的监听器。
     * 这个很重要，需要使用这里接口实现人机交互。
     * 有三种listener可选。详细参考asr模块下的recog.listener
     */
    IRecogListener recogListener = new StatusRecogListener() {
        @Override
        public void onAsrReady() {
            broadcastEvent(VoiceListener.EVENT_ASR_READY,null);
        }
        @Override
        public void onAsrPartialResult(String[] results, RecogResult recogResult) {
            StringBuilder sb = new StringBuilder();
            for (String word : results
            ) {
                sb.append(word);
            }
            broadcastEvent(VoiceListener.EVENT_ASR_PARTIAL_RESULT,sb.toString());
        }
        @Override
        public void onAsrFinalResult(String[] results, RecogResult recogResult) {

            StringBuilder sb = new StringBuilder();
            for (String word : results
            ) {
                sb.append(word);
            }

            broadcastEvent(VoiceListener.EVENT_ASR_FINAL_RESULT,sb.toString());
        }
        @Override
        public void onAsrFinish(RecogResult recogResult) {
            broadcastEvent(VoiceListener.EVENT_ASR_FINISH,null);
        }

    };


    protected void asrStart() {
        // DEMO集成步骤2.1 拼接识别参数： 此处params可以打印出来，直接写到你的代码里去，最终的json一致即可。
        final Map<String, Object> params = new LinkedHashMap<String, Object>();
        // 基于SDK集成2.1 设置识别参数
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        // params 也可以根据文档此处手动修改，参数会以json的格式在界面和logcat日志中打印
        Log.i(TAG, "设置的start输入参数：" + params);
        // DEMO集成步骤2.2 开始识别
        mAsr.start(params);
    }

}
