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
public class FavoriteVideo {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer vid;    // 视频ID
    private Integer fid;    // 收藏夹ID
    private Date time;  // 收藏时间
    private Integer isRemove;   // 是否移除 1已移出收藏夹
}
