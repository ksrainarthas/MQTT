/**
 * @项目名称: InteractionAssistant
 * @文件名称: ActionListener.java
 * @开发人员: 于交龙
 * @创建日期: 2016年4月19日
 */
package com.syusuke.mqtt.mqtt;

import android.os.Bundle;
import android.util.Log;

import com.jeremyliao.liveeventbus.LiveEventBus;
import com.syusuke.mqtt.bean.ValueBean;
import com.syusuke.mqtt.config.BundleKey;
import com.syusuke.mqtt.config.LiveEventBusConfig;
import com.syusuke.mqtt.config.ServiceAction;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

/**
 * MQTT动作监听类
 * <p>
 * by 于交龙 at 2016-04-19
 */
public class ActionListener implements IMqttActionListener {

    public static final String TAG = "MQTTDemo";
    private Bundle mBundle = null;


    public ActionListener(Bundle bundle) {
        mBundle = bundle;
    }

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        String action = mBundle.getString(BundleKey.ACTION);
        int index = mBundle.getInt(BundleKey.INDEX);
        Log.d(TAG, "action:" + action + "操作成功" + ",index:" + index);
        if (action.equals(ServiceAction.CONNECT)) {
            LiveEventBus.get(LiveEventBusConfig.MQTT).post(new ValueBean(ServiceAction.CONNECT, true, mBundle));
        } else if (action.equals(ServiceAction.DISCONNECT)) {
            LiveEventBus.get(LiveEventBusConfig.MQTT).post(new ValueBean(ServiceAction.DISCONNECT, true, mBundle));
        } else if (action.equals(ServiceAction.SUBSCRIBE)) {
            LiveEventBus.get(LiveEventBusConfig.MQTT).post(new ValueBean(ServiceAction.SUBSCRIBE, true, mBundle));
        } else if (action.equals(ServiceAction.PUBLISH)) {
            LiveEventBus.get(LiveEventBusConfig.MQTT).post(new ValueBean(ServiceAction.PUBLISH, true, mBundle));
        } else if (action.equals(ServiceAction.UNSUBSCRIBE)) {
            LiveEventBus.get(LiveEventBusConfig.MQTT).post(new ValueBean(ServiceAction.UNSUBSCRIBE, true, mBundle));
        }
    }

    @Override
    public void onFailure(IMqttToken token, Throwable exception) {
        String action = mBundle.getString(BundleKey.ACTION);
        Log.d(TAG, "action:" + action + "操作失败");
        if (action.equals(ServiceAction.CONNECT)) {
            LiveEventBus.get(LiveEventBusConfig.MQTT).post(new ValueBean(ServiceAction.CONNECT, false, mBundle));
        } else if (action.equals(ServiceAction.DISCONNECT)) {
            LiveEventBus.get(LiveEventBusConfig.MQTT).post(new ValueBean(ServiceAction.DISCONNECT, false, mBundle));
        } else if (action.equals(ServiceAction.SUBSCRIBE)) {
            LiveEventBus.get(LiveEventBusConfig.MQTT).post(new ValueBean(ServiceAction.SUBSCRIBE, false, mBundle));
        } else if (action.equals(ServiceAction.PUBLISH)) {
            LiveEventBus.get(LiveEventBusConfig.MQTT).post(new ValueBean(ServiceAction.PUBLISH, false, mBundle));
        } else if (action.equals(ServiceAction.UNSUBSCRIBE)) {
            LiveEventBus.get(LiveEventBusConfig.MQTT).post(new ValueBean(ServiceAction.UNSUBSCRIBE, false, mBundle));
        }
    }
}