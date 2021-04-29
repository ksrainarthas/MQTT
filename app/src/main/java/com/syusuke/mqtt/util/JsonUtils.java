/**
 * @项目名称: InteractionAssistant
 * @文件名称: JsonUtils.java
 * @开发人员: 于交龙
 * @创建日期: 2016-03-30
 */

package com.syusuke.mqtt.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * Json数据生成/解析 工具类
 * <p>
 * by 于交龙 at 2016-03-30
 */
public class JsonUtils<T> {
    /**
     * 解析Json数据为类对象模型
     *
     * @param json       要解析的Json数据
     * @param modelClass 要解析成的模型类
     * @return 模型类对象
     */
    public T parse(String json, Class<T> modelClass) {
        try {
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            return gson.fromJson(json, modelClass);
        } catch (JsonSyntaxException e) {
//            JLogUtils.d(e.toString());
        }

        return null;
    }

    /**
     * 将模型对象解析为Json数据
     *
     * @param model 模型对象
     * @return 解析后的Json数据
     */
    public String parse(T model) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(model);
    }
}
