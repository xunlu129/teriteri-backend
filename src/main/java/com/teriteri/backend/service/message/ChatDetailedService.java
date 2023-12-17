package com.teriteri.backend.service.message;

import java.util.Map;

public interface ChatDetailedService {
    /**
     * 获取当前聊天的20条消息
     * @param uid   发消息者UID（对方）
     * @param aid   收消息者UID（自己）
     * @param offset    偏移量 从哪条开始数（已经查过了几条）
     * @param isDesc    是否降序
     * @return  消息列表以及是否还有更多 { list: List, more: boolean }
     */
    Map<String, Object> getDetails(Integer uid, Integer aid, Long offset, boolean isDesc);
}
