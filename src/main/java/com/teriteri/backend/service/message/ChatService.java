package com.teriteri.backend.service.message;

import com.teriteri.backend.pojo.Chat;

import java.util.List;
import java.util.Map;

public interface ChatService {
    /**
     * 创建聊天
     * @param from  发消息者UID (我打开对方的聊天框即对方是发消息者)
     * @param to    收消息者UID (我打开对方的聊天框即我是收消息者)
     * @return "已存在"/"新创建"
     */
    Map<String, Object> createChat(Integer from, Integer to);

    /**
     * 获取聊天列表 包含用户信息和最近一条聊天内容 每次查10个
     * @param uid   登录用户ID
     * @param offset    查询偏移量（最近聊天的第几个开始往后查）
     * @return  包含用户信息和最近一条聊天内容的聊天列表
     */
    List<Map<String, Object>> getChatListWithData(Integer uid, Long offset);

    /**
     * 获取单个聊天
     * @param from  发消息者UID
     * @param to    收消息者UID
     * @return  Chat对象
     */
    Chat getChat(Integer from, Integer to);

    /**
     * 移除聊天 并清除未读
     * @param from  发消息者UID（对方）
     * @param to    收消息者UID（自己）
     */
    void delChat(Integer from, Integer to);

    /**
     * 发送消息时更新对应聊天的未读数和时间
     * @param from  发送者ID（自己）
     * @param to    接受者ID（对方）
     * @return 返回对方是否在窗口
     */
    boolean updateChat(Integer from, Integer to);

    /**
     * 更新窗口为在线状态，顺便清除未读
     * @param from  发消息者UID（对方）
     * @param to    收消息者UID（自己）
     */
    void updateWhisperOnline(Integer from, Integer to);

    /**
     * 更新窗口为离开状态
     * @param from  发消息者UID（对方）
     * @param to    收消息者UID（自己）
     */
    void updateWhisperOutline(Integer from, Integer to);
}
