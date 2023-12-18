package com.teriteri.backend.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommandType {
    /**
     * 建立连接
     */
    CONNETION(100),

    /**
     * 聊天功能 发送
     */
    CHAT_SEND(101),

    /**
     * 聊天功能 撤回
     */
    CHAT_WITHDRAW(102),

    ERROR(-1),
    ;

    private final Integer code;

    public static CommandType match(Integer code) {
        for (CommandType value: CommandType.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return ERROR;
    }
}
