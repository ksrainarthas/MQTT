package com.syusuke.mqtt.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.jeremyliao.liveeventbus.LiveEventBus;
import com.syusuke.mqtt.config.LiveEventBusConfig;
import com.syusuke.mqtt.util.VibrateUtils;

/**
 * Created by  on 2021/4/2.
 */
public class WifiStateChangedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String changed = "变化监听";
        switch (action) {
            case WifiManager.NETWORK_STATE_CHANGED_ACTION:
//                changed = "网络状态变化";
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (networkInfo != null) {
                    boolean connected = networkInfo.isConnected();
                    if (connected) {
                        changed = "wifi已连接";
                        VibrateUtils.starVibrate(context, new long[]{200, 300, 200, 300}, false);
                    } else {
                        changed = "wifi已断开";
                    }
                }

                LiveEventBus.get(LiveEventBusConfig.WIFI_STATE).post(changed);
                break;
            case WifiManager.WIFI_STATE_CHANGED_ACTION:
//                changed = "wifi状态变化";
                break;
            case WifiManager.RSSI_CHANGED_ACTION:
//                changed = "RSSI 变化";
                break;
        }
    }
}