package com.teriteri.backend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotSearch {
    private String content; // 内容
    private Double score;   // 热度
    private Integer type = 0;   // 类型： 0 普通 1 新 2 热
}
