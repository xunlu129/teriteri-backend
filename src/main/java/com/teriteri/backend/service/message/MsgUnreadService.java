package com.teriteri.backend.service.message;

import com.teriteri.backend.pojo.MsgUnread;

public interface MsgUnreadService {

    /**
     * 给指定用户的某一列未读消息加一
     * @param uid   用户ID
     * @param column    msg_unread表列名 "reply"/"at"/"love"/"system"/"whisper"/"dynamic"
     */
    void addOneUnread(Integer uid, String column);

    /**
     * 清除指定用户的某一列未读消息
     * @param uid   用户ID
     * @param column    msg_unread表列名 "reply"/"at"/"love"/"system"/"whisper"/"dynamic"
     */
    void clearUnread(Integer uid, String column);

    /**
     * 私聊消息特有的减除一定数量的未读数
     * @param uid   用户ID
     * @param count 要减多少
     */
    void subtractWhisper(Integer uid, Integer count);

    /**
     * 获取某人的全部消息未读数
     * @param uid   用户ID
     * @return  MsgUnread对象
     */
    MsgUnread getUnread(Integer uid);
}
