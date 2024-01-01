package com.teriteri.backend.service.message;

import java.util.Map;

public interface ChatDetailedService {
    /**
     * 获取当前聊天的20条消息
     * @param uid   发消息者UID（对方）
     * @param aid   收消息者UID（自己）
     * @param offset    偏移量 从哪条开始数（已经查过了几条）
     * @return  消息列表以及是否还有更多 { list: List, more: boolean }
     */
    Map<String, Object> getDetails(Integer uid, Integer aid, Long offset);

    /**
     * 删除单条消息记录
     * @param id 消息记录的id
     * @param uid   当前登录用户的UID
     * @return  成功/失败
     */
    boolean deleteDetail(Integer id, Integer uid);
}
