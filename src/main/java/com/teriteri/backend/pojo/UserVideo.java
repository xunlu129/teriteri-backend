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
public class UserVideo {
    @TableId(type = IdType.AUTO)
    private Integer id; // 唯一标识
    private Integer uid;    // 观看视频的用户ID
    private Integer vid;    // 视频ID
    private Integer play;   // 观看次数
    private Integer love;   // 点赞 0没赞 1已点赞
    private Integer unlove;    // 不喜欢 0没点 1已不喜欢
    private Integer coin;   // 投币数 0-2 一个视频一个用户上限投2个币
    private Integer collect;    // 收藏 0未收藏 1已收藏
    private Date playTime;    // 最近观看时间
    private Date loveTime;    // 最近点赞时间
    private Date coinTime;    // 最近投币时间
}
