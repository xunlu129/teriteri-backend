package com.teriteri.backend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MainCategory {
    private String mcId;
    private String mcName;
    private List<SubCategory> scList; // 关联的附分类对象列表
}
