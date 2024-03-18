package com.teriteri.backend.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Integer uid;
    private String nickname;
    private String avatar_url;
    private String bg_url;
    private Integer gender; // 性别，0女性 1男性 2无性别，默认2
    private String description;
    private Integer exp;    // 经验值 50/200/1500/4500/10800/28800 分别是0~6级的区间
    private Double coin;    // 硬币数  保留一位小数
    private Integer vip;    // 0 普通用户，1 月度大会员，2 季度大会员，3 年度大会员
    private Integer state;  // 0 正常，1 封禁中
    private Integer auth;   // 0 普通用户，1 个人认证，2 机构认证
    private String authMsg; // 认证信息，如 teriteri官方账号
    private Integer videoCount; // 视频投稿数
    private Integer followsCount;   // 关注数
    private Integer fansCount;  // 粉丝数
    private Integer loveCount;  // 获赞数
    private Integer playCount;  // 播放数
}
