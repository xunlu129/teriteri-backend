package com.teriteri.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Favorite {
    @TableId(type = IdType.AUTO)
    private Integer fid;    // 收藏夹ID
    private Integer uid;    // 所属用户ID
    private Integer type;   // 收藏夹类型 1默认收藏夹 2用户创建
    private Integer visible;    // 对外开放 0隐藏 1公开
    private String cover;   // 收藏夹封面url
    private String title;   // 收藏夹名称
    private String description; // 简介
    private Integer count;  // 收藏夹中视频数量
    private Integer isDelete;   // 是否删除 1已删除
}
