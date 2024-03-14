package com.teriteri.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoStats {
    @TableId
    private Integer vid;
    private Integer play;
    private Integer danmu;
    private Integer good;
    private Integer bad;
    private Integer coin;
    private Integer collect;
    private Integer share;
    private Integer comment;
}
