/**
 * @项目名称: InteractionAssistant
 * @文件名称: CallbackHandler.java
 * @开发人员: 于交龙
 * @创建日期: 2016年4月19日
 */
package com.syusuke.mqtt.mqtt;

import android.content.Context;
import android.util.Log;

import com.jeremyliao.liveeventbus.LiveEventBus;
import com.syusuke.mqtt.config.LiveEventBusConfig;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * MQTT回调处理类
 * <p>
 * by 于交龙 at 2016-04-19
 */
public class CallbackHandler implements MqttCallbackExtended {
    public static final String TAG = "MQTTDemo";
    private Context mContext = null;
    private int index;

    public CallbackHandler(Context context, int index) {
        mContext = context;
        this.index = index;
    }

    @Override
    public void connectionLost(Throwable cause) {
        // 连接断开,提示用户正在重新连接服务器
        Log.d(TAG, "断开连接");
        LiveEventBus.get(LiveEventBusConfig.MQTT_CONNECT_LOST).post(index);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        // 消息到达,解析并派发给UI线程展示处理
        String msg = new String(message.getPayload());

        Log.d(TAG, "接收到主题为:" + topic + "的消息");
        Log.d(TAG, msg);

        // 处理消息时，阻塞后续收到的消息
        synchronized (CallbackHandler.class) {
            handleMsg(msg);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // 消息发送成功,不作处理
    }

    /**
     * 处理接收到的消息
     *
     * @param msg 要处理的消息
     */
    private void handleMsg(String msg) {
        Log.d(TAG, "处理消息: " + msg);
        LiveEventBus.get(LiveEventBusConfig.MQTT_MESSAGE).post(msg);
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        Log.d(TAG, "MQTT 连接成功" + index);
    }
}
