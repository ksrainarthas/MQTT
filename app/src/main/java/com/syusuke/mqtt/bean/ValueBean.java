package com.syusuke.mqtt.bean;

import android.os.Bundle;

/**
 * Created by  on 2021/4/1.
 */
public class ValueBean {
    private String action;
    private boolean isSuccess;
    private Bundle bundle;

    public ValueBean(String action, boolean isSuccess, Bundle bundle) {
        this.action = action;
        this.isSuccess = isSuccess;
        this.bundle = bundle;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }
}