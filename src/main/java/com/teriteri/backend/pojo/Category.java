package com.teriteri.backend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    private String mcId;
    private String scId;
    private String mcName;
    private String scName;
    private String descr;
    private String rcmTag;
}
