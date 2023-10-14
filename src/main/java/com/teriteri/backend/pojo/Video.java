package com.teriteri.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Video {
    @TableId(type = IdType.AUTO)
    private Integer vid;
    private Integer uid;
    private String title;
    private Integer type;
    private Integer auth;
    private Double duration;
    private String mcId;
    private String scId;
    private String tags;
    private String descr;
    private String coverUrl;
    private String videoUrl;
    private Integer status;     // 0审核中 1通过审核 2其他
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private Date uploadDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private Date deleteDate;
}
