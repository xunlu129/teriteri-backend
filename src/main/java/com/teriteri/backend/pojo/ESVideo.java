package com.teriteri.backend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ESVideo {
    private Integer vid;
    private Integer uid;
    private String title;
    private String mc_id;
    private String sc_id;
    private String tags;
    private Integer status;
}
