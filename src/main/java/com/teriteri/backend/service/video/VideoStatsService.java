package com.teriteri.backend.service.video;

import com.teriteri.backend.pojo.VideoStats;

public interface VideoStatsService {
    VideoStats getVideoStatsById(Integer vid);
}
