package com.syusuke.mqtt.config;

/**
 * Created by  on 2021/4/1.
 */
public class ServiceAction {
    /**
     * 连接交互服务器
     */
    public static final String CONNECT = "CONNECT";

    /**
     * 连接交互服务器
     */
    public static final String LOGIN = "LOGIN";

    /**
     * 重连服务器
     */
    public static final String RELOGIN = "RELOGIN";

    /**
     * 断开交互服务器
     */
    public static final String DISCONNECT = "DISCONNECT";

    /**
     * 订阅主题
     */
    public static final String SUBSCRIBE = "SUBSCRIBE";

    /**
     * 取消订阅主题
     */
    public static final String UNSUBSCRIBE = "UNSUBSCRIBE";

    /**
     * 发送消息
     */
    public static final String PUBLISH = "PUBLISH";

    /**
     * 启动保持连接
     */
    public static final String START_KEEP_ALIVE = "START_KEEP";

    /**
     * 停止保持连接
     */
    public static final String STOP_KEEP_ALIVE = "STOP_KEEP";

    /**
     * 保持连接
     */
    public static final String KEEP_ALIVE = "KEEP";

    /**
     * 停止服务
     */
    public static final String STOP_SERVICE = "STOP";
}