package com.teriteri.backend.service.video;

import com.teriteri.backend.pojo.CustomResponse;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface VideoService {
    /**
     * 根据id分页获取视频信息，包括用户和分区信息
     * @param set   要查询的视频id集合
     * @param index 分页页码 为空默认是1
     * @param quantity  每一页查询的数量 为空默认是10
     * @return  包含用户信息、分区信息、视频信息的map列表
     */
    List<Map<String, Object>> getVideosWithDataByIds(Set<Object> set, Integer index, Integer quantity);

    /**
     * 按指定列（倒序）排序或者按给定id顺序排序，分页获取视频信息，
     * 允许查询已删除的视频，已删除的视频信息会经过筛选处理
     * @param idList   要查询的视频id列表，column为null时按该列表顺序排序
     * @param column    要排序的列   可选：null/"upload_date"/"play"/"good"/...
     * @param page  分页页码 从1开始
     * @param quantity  每一页查询的数量
     * @return  包含用户信息、分区信息、视频信息的map顺序列表
     */
    List<Map<String, Object>> getVideosWithDataByIdsOrderByDesc(List<Integer> idList, @Nullable String column, Integer page, Integer quantity);

    /**
     * 根据vid查询单个视频信息，包含用户信息和分区信息
     * @param vid 视频ID
     * @return 包含用户信息、分区信息、视频信息的map
     */
    Map<String, Object> getVideoWithDataById(Integer vid);

    /**
     * 根据有序vid列表查询视频以及相关信息
     * @param list  vid有序列表
     * @return  有序的视频列表
     */
    List<Map<String, Object>> getVideosWithDataByIdList(List<Integer> list);

    /**
     * 更新视频状态，包括过审、不通过、删除，其中审核相关需要管理员权限，删除可以是管理员或者投稿用户
     * @param vid   视频ID
     * @param status 要修改的状态，1通过 2不通过 3删除
     * @return 无data返回，仅返回响应信息
     */
    CustomResponse updateVideoStatus(Integer vid, Integer status) throws IOException;
}
