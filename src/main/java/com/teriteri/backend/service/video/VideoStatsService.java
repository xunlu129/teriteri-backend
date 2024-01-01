package com.teriteri.backend.service.video;

import com.teriteri.backend.pojo.VideoStats;

public interface VideoStatsService {
    /**
     * 根据视频ID查询视频常变数据
     * @param vid 视频ID
     * @return
     */
    VideoStats getVideoStatsById(Integer vid);
}
