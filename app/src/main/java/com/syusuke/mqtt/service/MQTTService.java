package com.syusuke.mqtt.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.Nullable;

import com.jeremyliao.liveeventbus.LiveEventBus;
import com.syusuke.mqtt.config.BundleKey;
import com.syusuke.mqtt.config.GlobalConfig;
import com.syusuke.mqtt.config.LiveEventBusConfig;
import com.syusuke.mqtt.config.ServiceAction;
import com.syusuke.mqtt.mqtt.ActionListener;
import com.syusuke.mqtt.mqtt.AndroidClient;
import com.syusuke.mqtt.mqtt.CallbackHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by  on 2021/4/1.
 */
public class MQTTService extends Service {
    public static final String TAG = "MQTTDemo";
    //    private AndroidClient androidClient;
    private SparseArray<AndroidClient> androidClients = new SparseArray<>();
    private String server;
    private int port;
    private String[] clientIds;
    private String[] users;
    private AsyncTask mAsyncTask;
    private static final int reLimit = 1000000;
//    private int reCount = 0;
    private SparseArray<Integer> reCounts = new SparseArray<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String action = bundle.getString(BundleKey.ACTION);
                if (action != null) {
                    switch (action) {
                        case ServiceAction.LOGIN:
                            server = bundle.getString(BundleKey.SERVER);
                            port = bundle.getInt(BundleKey.PORT);
                            clientIds = bundle.getStringArray(BundleKey.CLIENT_IDS);
                            users = bundle.getStringArray(BundleKey.USERS);
                            for (int i = 0; i < clientIds.length; i++) {
                                if (GlobalConfig.isPing) {
                                    connect(i);
                                } else {
                                    connect2(i);
                                }
                            }
                            break;
                        case ServiceAction.RELOGIN:
                            int index = bundle.getInt(BundleKey.INDEX);
                            if (GlobalConfig.isPing) {
                                connect(index);
                            } else {
                                connect2(index);
                            }
                            break;
                        case ServiceAction.CONNECT:
                            index = bundle.getInt(BundleKey.INDEX);
                            try {
                                AndroidClient androidClient = androidClients.get(index);
                                if (androidClient == null) {
                                    androidClient = new AndroidClient(MQTTService.this, server, port, clientIds[index], users[index]);
                                    androidClients.put(index, androidClient);
                                }
                                if (!androidClient.isConnected(this)) {
                                    androidClient.connect(new ActionListener(bundle), new CallbackHandler(MQTTService.this, index));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case ServiceAction.DISCONNECT:
                            try {
                                index = bundle.getInt(BundleKey.INDEX);
                                androidClients.get(index).disconnect(new ActionListener(bundle));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case ServiceAction.SUBSCRIBE:
                            String topic = bundle.getString(BundleKey.TOPIC);
                            int qos = bundle.getInt(BundleKey.QOS);
                            index = bundle.getInt(BundleKey.INDEX);
                            if (androidClients.get(index) != null) {
                                try {
                                    if (topic != null && topic.contains(",")) {
                                        String[] topics = topic.split(",");
                                        int[] qoss = new int[topics.length];
                                        for (int i = 0; i < topics.length; i++) {
                                            qoss[i] = qos;
                                        }
                                        androidClients.get(index).subscribe(topics, qoss, new ActionListener(bundle));
                                    } else {
                                        androidClients.get(index).subscribe(topic, qos, new ActionListener(bundle));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        case ServiceAction.PUBLISH:
                            topic = bundle.getString(BundleKey.TOPIC);
                            String message = bundle.getString(BundleKey.MESSAGE);
                            index = bundle.getInt(BundleKey.INDEX);
                            try {
                                if (androidClients.get(index) != null) {
                                    androidClients.get(index).publish(topic, message, new ActionListener(bundle));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case ServiceAction.UNSUBSCRIBE:
                            topic = bundle.getString(BundleKey.TOPIC);
                            index = bundle.getInt(BundleKey.INDEX);
                            try {
                                if (androidClients.get(index) != null) {
                                    androidClients.get(index).unsubscribe(topic, new ActionListener(bundle));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case ServiceAction.STOP_SERVICE:
                            // 如果此时异步任务正在运行, 则停止
                            if (mAsyncTask != null && mAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                                mAsyncTask.cancel(true);
                                Log.d(TAG, "由于服务停止, 异步任务被杀死");
                            }
                            for (int i = 0; i < reCounts.size(); i++) {
                                reCounts.put(i, 0);
                            }
                            stopSelf();
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        return Service.START_STICKY;
    }

    private void connect2(int index) {
        Bundle bundle = new Bundle();
        bundle.putString(BundleKey.ACTION, ServiceAction.CONNECT);
        bundle.putString(BundleKey.SERVER, server);
        bundle.putInt(BundleKey.PORT, port);
        bundle.putInt(BundleKey.INDEX, index);
        bundle.putString(BundleKey.CLIENT_ID, clientIds[index]);
        bundle.putString(BundleKey.USER, users[index]);
        Intent intent = new Intent(this, MQTTService.class);
        intent.putExtras(bundle);
        startService(intent);
    }

    @SuppressLint("StaticFieldLeak")
    private void connect(final int index) {
        mAsyncTask = new AsyncTask<Void, String, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                boolean success = ping(server);
                if (success) {
                    // 链接成功
                    publishProgress("@Success");
                } else {
                    publishProgress("@Failure");
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(String... values) {
                if (values[0].equals("@Success")) {
                    Log.d(TAG, "socket连接上" + server + ",index=" + index);
                    reCounts.put(index, 0);
                    Bundle bundle = new Bundle();
                    bundle.putString(BundleKey.ACTION, ServiceAction.CONNECT);
                    bundle.putString(BundleKey.SERVER, server);
                    bundle.putInt(BundleKey.PORT, port);
                    bundle.putInt(BundleKey.INDEX, index);
                    bundle.putString(BundleKey.CLIENT_ID, clientIds[index]);
                    bundle.putString(BundleKey.USER, users[index]);
                    Intent intent = new Intent(MQTTService.this, MQTTService.class);
                    intent.putExtras(bundle);
                    startService(intent);
                    LiveEventBus.get(LiveEventBusConfig.PING).post("socket连接上" + server + ",index=" + index);
                } else {
                    Log.d(TAG, "socket连接失败" + server + ",index=" + index);
                    LiveEventBus.get(LiveEventBusConfig.PING).post("socket连接失败" + server + ",index=" + index);
                    if (reCounts.get(index) < reLimit) {
                        Timer timer = new Timer();
                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {
                                //重连
                                Bundle bundle = new Bundle();
                                bundle.putString(BundleKey.ACTION, ServiceAction.RELOGIN);
                                bundle.putInt(BundleKey.INDEX, index);
                                Intent intent = new Intent(MQTTService.this, MQTTService.class);
                                intent.putExtras(bundle);
                                startService(intent);
                                Integer integer = reCounts.get(index);
                                integer ++;
                                reCounts.put(index, integer);
                                String format = String.format(Locale.getDefault(), "客户端%1$d重连第%2$d次", index, integer);
                                LiveEventBus.get(LiveEventBusConfig.PING).post(format);
                                Log.d(TAG, format);
                            }
                        };
                        timer.schedule(task, 5000);
                    } else {
                        //超过次数 不再重连
                        Log.d(TAG, "超过次数 不再重连socket");
                        LiveEventBus.get(LiveEventBusConfig.PING).post("超过次数 不再重连socket");
                        reCounts.put(index, 0);
                    }
                }
            }
        }.execute();
    }

    /**
     * socket 尝试连接ip
     */
    public boolean ping(String ip) {
        try {
            Socket socket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(ip, port);
            socket.connect(socketAddress, 1000);//连接请求超时时间
            socket.setSoTimeout(300);//设置读操作超时时间
            socket.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void onDestroy() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < androidClients.size(); i++) {
                    if (androidClients.get(i) != null) {
                        androidClients.get(i).release();
                    }
                }
                androidClients.clear();
            }
        }, 1000);
        super.onDestroy();
    }
}