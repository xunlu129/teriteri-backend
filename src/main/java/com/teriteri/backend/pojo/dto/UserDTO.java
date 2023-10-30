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
    private Integer gender; // 性别，0女性 1男性 2无性别，默认2
    private String description;
    private Integer exp;    // 经验值 50/200/1500/4500/10800/28800 分别是0~6级的区间
    private Integer state;  // 0 正常，1 封禁中
}
