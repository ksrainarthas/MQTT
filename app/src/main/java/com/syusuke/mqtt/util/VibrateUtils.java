package com.syusuke.mqtt.util;

import android.app.Service;
import android.content.Context;
import android.os.Vibrator;

/**
 * Created by  on 2021/4/2.
 */
public class VibrateUtils {
    /**
     * @param pattern  震动频率
     * @param isRepeat 是否循环执行震动
     */
    public static void starVibrate(Context context, long[] pattern, boolean isRepeat) {
        Vibrator vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(pattern, isRepeat ? 1 : -1);
    }

    public static void stopVibrate(Context context) {
        Vibrator vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        vib.cancel();
    }
}