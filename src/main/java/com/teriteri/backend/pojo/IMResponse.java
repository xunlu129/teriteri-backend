package com.teriteri.backend.pojo;

import com.alibaba.fastjson2.JSON;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IMResponse {
    private String type;    // 消息类型 如："reply","at","love","system","whisper","dynamic" / "error"
    private LocalDateTime time;
    private Object data;    // 返回的相关数据

    /**
     * 返回系统失败消息
     * @param message   自定义系统消息提示
     * @return 返回系统失败消息
     */
    public static TextWebSocketFrame error(String message) {
        return new TextWebSocketFrame(JSON.toJSONString(new IMResponse("error", LocalDateTime.now(), message)));
    }

    /**
     * 非系统类消息
     * @param type  消息类型 如："reply","at","love","system","whisper","dynamic" 对应 msg_unread 表的每一列
     * @param data  返回的相关数据
     * @return  返回非系统消息以及携带数据
     */
    public static TextWebSocketFrame message(String type, Object data) {
        return new TextWebSocketFrame(JSON.toJSONString(new IMResponse(type, LocalDateTime.now(), data)));
    }
}
