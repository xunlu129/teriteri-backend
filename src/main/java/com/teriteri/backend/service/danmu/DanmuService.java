package com.teriteri.backend.service.danmu;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.Danmu;

import java.util.List;
import java.util.Set;

public interface DanmuService {
    /**
     * 根据弹幕ID集合查询弹幕列表
     * @param idset 弹幕ID集合
     * @return  弹幕列表
     */
    List<Danmu> getDanmuListByIdset(Set<Object> idset);

    /**
     * 删除弹幕
     * @param id    弹幕id
     * @param uid   操作用户
     * @param isAdmin   是否管理员
     * @return  响应对象
     */
    CustomResponse deleteDanmu(Integer id, Integer uid, boolean isAdmin);
}
