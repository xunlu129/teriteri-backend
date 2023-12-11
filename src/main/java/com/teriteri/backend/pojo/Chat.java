package com.teriteri.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Chat {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;     // 发送者UID
    private Integer anotherId;  // 接收者UID
    private Integer isDeleted;  // 是否移除聊天 0否 1是
    private Integer unread;     // 消息未读数
    private Date latestTime;    // 最近接收消息的时间或最近打开聊天窗口的时间

}
