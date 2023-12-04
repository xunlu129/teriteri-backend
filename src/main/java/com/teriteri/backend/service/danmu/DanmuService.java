package com.teriteri.backend.service.danmu;

import com.teriteri.backend.pojo.Danmu;

import java.util.List;
import java.util.Set;

public interface DanmuService {
    List<Danmu> getDanmuListByIdset(Set<Object> idset);
}
