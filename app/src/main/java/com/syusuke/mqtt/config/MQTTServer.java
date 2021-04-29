package com.syusuke.mqtt.config;

/**
 * Created by  on 2021/4/1.
 */
public class MQTTServer {
    /**
     * 协议
     */
    public static final String PROTOCOL = "tcp://";

    public static final String HOST = "192.168.100.8";
    public static final int PORT = 1883;
    public static final String USER_NAME = "admin";
    public static final String PASSWORD = "password";

    /**
     * 连接MQTT服务器超时(单位:秒)
     */
    public static final int CONNECT_TIMEOUT = 5;

    /**
     * 心跳检测时间(单位:秒)
     */
    public static final int KEEP_ALIVE = 30;

    /**
     * 服务质量
     */
    public static final int DEFUALT_QOS = 2;

    /**
     * 服务器端是否保留订阅主题的最后一条消息
     */
    public static final boolean DEFUALT_RETAINED = false;

    /**
     * 是否清除会话
     */
    public static boolean sCleanSession = false;

    /**
     * 服务器主题
     */
    public static final String SERVER_TOPIC = "server_topic";

    /**
     * 班级主题
     */
    public static final String CLASS_TOPIC = "class_topic";

    /**
     * 数据缓存还原主题
     */
    public static final String RESTORE_TOPIC = "restore_topic";

    /**
     * 电子白板接收消息主题
     */
    public static final String WHITEBOARD_GETTOPIC = "whiteboard_gettopic";

    /**
     * 电子白板发送消息主题
     */
    public static final String WHITEBOARD_SENDTOPIC = "whiteboard_sendtopic";

    /**
     * LastWill 主题
     */
    public static final String LAST_WILL_TOPIC = WHITEBOARD_GETTOPIC;
    public static final int LAST_WILL_QOS = 2;
    public static final boolean LAST_WILL_RETAINED = false;

    /**
     * 断开连接前的最后处理时间(单位:毫秒)
     */
    public static final int QUIESCE_TIMEOUT = 1000;
}