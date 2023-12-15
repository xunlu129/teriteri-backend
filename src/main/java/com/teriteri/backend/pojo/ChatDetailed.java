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
public class ChatDetailed {
    @TableId(type = IdType.AUTO)
    private Integer id;         // 消息id
    private Integer userId;     // 发送者uid
    private Integer anotherId;  // 接受者uid
    private String content;     // 消息内容
    private Integer userDel;    // 发送者是否删除
    private Integer anotherDel; // 接受者者是否删除
    private Integer withdraw;   // 消息是否被撤回
    private Date time;          // 发送消息的时间
}
