package com.syusuke.mqtt.mqtt;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import com.syusuke.mqtt.bean.LastWill;
import com.syusuke.mqtt.config.BundleKey;
import com.syusuke.mqtt.config.MQTTServer;
import com.syusuke.mqtt.config.ServiceAction;
import com.syusuke.mqtt.util.JsonUtils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

/**
 * Created by  on 2021/4/1.
 */
public class AndroidClient {
    public static final String TAG = "MQTTDemo";
    private static AndroidClient mAndroidClient = null;
    private MqttConnectOptions mDefualtConOpt = null;
    private MqttAndroidClient mMqttAndroidClient = null;

    /**
     * 获取AndroidClient唯一实例
     *
     * @param context  当前应用的context
     * @param server   服务器IP
     * @param port     服务器端口号
     * @param clientId Mqtt clientid
     * @param user     用户名
     * @return AndroidClient唯一实例
     */
    public static AndroidClient getInstance(Context context, String server, int port, String clientId, String user) {
        if (mAndroidClient == null) {
            synchronized (AndroidClient.class) {
                if (mAndroidClient == null) {
                    mAndroidClient = new AndroidClient(context, server, port, clientId, user);
                }
            }
        }

        return mAndroidClient;
    }

    public AndroidClient(Context context, String server, int port, String clientId, String user) {
        initDefualtConOpt(user, clientId);
        mMqttAndroidClient = new MqttAndroidClient(context, MQTTServer.PROTOCOL + server + ":" + port, clientId);
    }

    /**
     * 初始化默认的连接配置
     *
     * @param lastWillMsg
     * @param clientId
     */
    private void initDefualtConOpt(String lastWillMsg, String clientId) {
        mDefualtConOpt = new MqttConnectOptions();
        mDefualtConOpt.setUserName(MQTTServer.USER_NAME);
        mDefualtConOpt.setPassword(MQTTServer.PASSWORD.toCharArray());
        mDefualtConOpt.setCleanSession(MQTTServer.sCleanSession);
        mDefualtConOpt.setConnectionTimeout(MQTTServer.CONNECT_TIMEOUT);
        mDefualtConOpt.setKeepAliveInterval(MQTTServer.KEEP_ALIVE);

        String lastWill = new JsonUtils<LastWill>().parse(new LastWill(lastWillMsg, clientId));

        mDefualtConOpt.setWill(MQTTServer.LAST_WILL_TOPIC, lastWill.getBytes(), MQTTServer.LAST_WILL_QOS,
                MQTTServer.LAST_WILL_RETAINED);
    }

    /**
     * 连接服务器
     *
     * @param listener 连接监听
     * @param callback MQTT事件回调
     */
    public void connect(IMqttActionListener listener, MqttCallback callback) throws Exception {
        mMqttAndroidClient.setCallback(callback);
        mMqttAndroidClient.connect(mDefualtConOpt, null, listener);
    }

    /**
     * 断开服务器连接
     *
     * @param listener 连接监听
     */
    public void disconnect(IMqttActionListener listener) throws Exception {
        mMqttAndroidClient.disconnect(MQTTServer.QUIESCE_TIMEOUT, null, listener);
    }

    /**
     * 订阅主题
     *
     * @param topic    要订阅的主题
     * @param qos      主题的QOS
     * @param listener 订阅监听
     */
    public void subscribe(String topic, int qos, IMqttActionListener listener) throws Exception {
        try {
            if (mMqttAndroidClient != null)// Oct 24,2018 非空判断
            {
                mMqttAndroidClient.subscribe(topic, qos, null, listener);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 订阅主题
     *
     * @param topic    要订阅的主题数组
     * @param qos      主题的QOS
     * @param listener 订阅监听
     */
    public void subscribe(String[] topic, int[] qos, IMqttActionListener listener) throws Exception {
        try {
            if (mMqttAndroidClient != null) {
                mMqttAndroidClient.subscribe(topic, qos, null, listener);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 取消订阅
     *
     * @param topic    要取消订阅的主题
     * @param listener 取消订阅监听
     */
    public void unsubscribe(String topic, IMqttActionListener listener) throws Exception {
        mMqttAndroidClient.unsubscribe(topic, null, listener);
    }

    /**
     * 取消订阅
     *
     * @param topic    要取消订阅的主题数组
     * @param listener 取消订阅监听
     */
    public void unsubscribe(String[] topic, IMqttActionListener listener) throws Exception {
        mMqttAndroidClient.unsubscribe(topic, null, listener);
    }

    /**
     * 向指定主题发布消息
     *
     * @param topic    主题
     * @param msg      消息
     * @param qos      QOS 服务质量
     * @param listener 发布消息监听
     */
    public void publish(String topic, String msg, int qos, IMqttActionListener listener) throws Exception {
        if (mMqttAndroidClient != null && msg != null && topic != null && listener != null) {
            Log.d(TAG, "向主题:" + topic + "发送消息, QOS =" + qos);

            byte[] msgBitArray = msg.getBytes();

            if (msgBitArray != null && mMqttAndroidClient != null) {
                try {
                    mMqttAndroidClient.publish(topic, msgBitArray, qos, MQTTServer.DEFUALT_RETAINED, "",
                            listener);
                } catch (Exception e) {
                    Log.d(TAG, "mqttService is null !!!");
                    e.printStackTrace();
                }
            } else {
                Log.d(TAG, "msgBitArray == null;" + "msg:" + msg);
            }
        }
    }

    /**
     * 向指定主题发布消息
     *
     * @param topic    主题
     * @param msg      消息
     * @param listener 发布消息监听
     */
    public void publish(String topic, String msg, IMqttActionListener listener) throws Exception {
        if (mMqttAndroidClient != null && msg != null && topic != null && listener != null) {
            Log.d(TAG, "向主题:" + topic + "发送消息");
            Log.d(TAG, msg);

            byte[] msgBitArray = msg.getBytes();

            if (msgBitArray != null && mMqttAndroidClient != null) {
                try {
                    mMqttAndroidClient.publish(topic, msgBitArray, MQTTServer.DEFUALT_QOS, MQTTServer.DEFUALT_RETAINED, "",
                            listener);
                } catch (Exception e) {
                    Log.d(TAG, "mqttService is null !!!");
                    e.printStackTrace();
                }
            } else {
                Log.d(TAG, "msgBitArray == null;" + "msg:" + msg);
            }
        }
    }

    /**
     * 释放MQTT资源
     */
    public void release() {
        if (mMqttAndroidClient != null) {
            mMqttAndroidClient.unregisterResources();

            try {
                Bundle bundle = new Bundle();
                bundle.putString(BundleKey.ACTION, ServiceAction.DISCONNECT);
                mMqttAndroidClient.disconnect(MQTTServer.QUIESCE_TIMEOUT, null, new ActionListener(bundle));
            } catch (Exception e) {
                Log.d(TAG, "MQTT异常, 异常信息:" + e.getMessage());
            } finally {
                mAndroidClient = null;
                mMqttAndroidClient = null;
                Log.d(TAG, "MQTT释放对象:mMqttAndroidClient = null");
            }
        }
    }

    public boolean isConnected(Context context) {
        return mMqttAndroidClient.isConnected() && isConnectIsNormal(context);
    }

    /** 判断网络是否连接 */
    private boolean isConnectIsNormal(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            Log.i(TAG, "MQTT当前网络名称：" + name);
            return true;
        } else {
            Log.i(TAG, "MQTT 没有可用网络");
            return false;
        }
    }
}