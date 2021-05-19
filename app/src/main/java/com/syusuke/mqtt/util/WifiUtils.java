package com.syusuke.mqtt.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.Objects;

/**
 * Created by  on 2021/4/2.
 */
public class WifiUtils {
    public static final String TAG = "MQTTDemo";

    //https://blog.csdn.net/always_and_forever_/article/details/81976291
    public static boolean isWifiConnect(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return Objects.requireNonNull(networkInfo).isConnected();
    }

    public static String checkWifiState(Context context) {
        if (isWifiConnect(context)) {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//            if (!wifiManager.isWifiEnabled()) {
//                if (wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLING) {
//                    wifiManager.setWifiEnabled(true);
//                }
//            }
            WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            int wifi = connectionInfo.getRssi();
            if (wifi >= -50 && wifi <= 0) {//最强
                return "最强" + wifi;
            } else if (wifi >= -70 && wifi < -50) {//较强
                return "较强" + wifi;
            } else if (wifi >= -80 && wifi < -70) {//较弱
                return "较弱" + wifi;
            } else if (wifi >= -100 && wifi < -80) {//微弱
                return "微弱" + wifi;
            } else {
                return "最弱" + wifi;
            }
        } else {
            return "无wifi连接";
        }
    }

    public static String eCheckWifi(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            if (wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLING) {
                wifiManager.setWifiEnabled(true);
            }
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null && wifiInfo.getBSSID() != null) {
            int signalLevel = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 4);
            return "信号强度:" + signalLevel;
        } else {
            return "无wifi连接";
        }
    }
}