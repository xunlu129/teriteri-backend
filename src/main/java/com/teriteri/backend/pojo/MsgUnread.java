package com.teriteri.backend.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MsgUnread {
    @TableId
    private Integer uid;    // 用户uid 不自动增长 跟随注册时的uid
    private Integer reply;  // 回复我的
    private Integer at;     // @ 我的
    private Integer love;   // 收到的赞
    @TableField("`system`")     // 开发失误，以后注意不要用这种关键字命名
    private Integer system; // 系统通知
    private Integer whisper;    // 我的消息（私聊总数）
    @TableField("`dynamic`")
    private Integer dynamic;    // 动态
}
