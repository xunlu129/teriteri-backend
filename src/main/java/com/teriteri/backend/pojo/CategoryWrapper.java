package com.teriteri.backend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 分区包装类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryWrapper {
    private String mcId;
    private String mcName;
    private List<Map<String, Object>> scList;
}
