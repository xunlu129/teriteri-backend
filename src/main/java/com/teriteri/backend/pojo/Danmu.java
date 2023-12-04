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
public class Danmu {
    @TableId(type = IdType.AUTO)
    private Integer id;     // 弹幕ID
    private Integer vid;    // 视频ID
    private Integer uid;    // 用户ID
    private String content; // 弹幕内容
    private Integer fontsize;   // 字体大小 默认25 小18
    private Integer mode;   // 模式 1滚动 2顶部 3底部
    private String color;   // 字体颜色 6位十六进制标准格式 #FFFFFF
    private Double timePoint;   // 弹幕在视频中的时间点位置（秒）
    private Integer state;  // 弹幕状态 1默认过审 2被举报审核中 3删除
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private Date createDate;    // 弹幕发送日期时间
}
