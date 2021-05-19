/**
 * @项目名称: InteractionAssistant
 * @文件名称: LastWill.java
 * @开发人员: 于交龙
 * @创建日期: 2016-03-29
 */
package com.syusuke.mqtt.bean;

/**
 * LastWill模型类
 * <p>
 * by 于交龙 at 2016-03-29
 */
public class LastWill {
    /**
     * 请求/响应 代码
     */
    private int code = 31;

    /**
     * 用户名
     */
    private String user = null;

    /**
     * 用户ID
     */
    private String id = null;

    public LastWill() {
    }

    public LastWill(String user, String id) {
        this.user = user;
        this.id = id;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
