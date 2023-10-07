package com.teriteri.backend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 响应包装类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
// 自定义响应对象
public class CustomResponse {
    private int code = 200;
    private String message = "OK";
    private Object data;
}
