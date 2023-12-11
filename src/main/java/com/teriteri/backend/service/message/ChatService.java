package com.teriteri.backend.service.message;

import java.util.List;
import java.util.Map;

public interface ChatService {
    /**
     * 创建聊天
     * @param from  发消息者UID
     * @param to    收消息者UID
     * @return "已存在"/"新创建"
     */
    Map<String, Object> createChat(Integer from, Integer to);

    /**
     * 获取聊天列表 包含用户信息和最近一条聊天内容 每次查10个
     * @param uid   用户ID
     * @param offset    查询偏移量（最近聊天的第几个开始往后查）
     * @return  包含用户信息和最近一条聊天内容的聊天列表
     */
    List<Map<String, Object>> getChatListWithData(Integer uid, Long offset);

    /**
     * 移除聊天
     * @param from  发消息者UID
     * @param to    收消息者UID
     */
    void delChat(Integer from, Integer to);
}
