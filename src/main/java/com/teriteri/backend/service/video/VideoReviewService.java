package com.teriteri.backend.service.video;

import com.teriteri.backend.pojo.CustomResponse;

public interface VideoReviewService {
    /**
     * 查询对应状态的视频数量
     * @param status 状态 0审核中 1通过审核 2打回整改（指投稿信息不符） 3视频违规删除（视频内容违规）
     * @return 包含视频数量的CustomResponse对象
     */
    CustomResponse getTotalByStatus(Integer status);

    /**
     * 获取分页对应状态的视频
     * @return CustomResponse对象，包含符合条件的视频列表
     */
    CustomResponse getVideosByPage(Integer status, Integer page, Integer quantity);
}
