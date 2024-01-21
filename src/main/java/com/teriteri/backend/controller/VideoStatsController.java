package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.service.video.VideoStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VideoStatsController {
    @Autowired
    private VideoStatsService videoStatsService;

    /**
     * 游客观看视频时更新视频播放量数据，这个做不到时间间隔，就是说每次刷新都会播放数加一，有一个思路是使用浏览器指纹，但是我不会
     * @param vid 视频ID
     * @return
     */
    @PostMapping("/video/play/visitor")
    public CustomResponse newPlayWithVisitor(@RequestParam("vid") Integer vid) {
        videoStatsService.updateStats(vid, "play", true, 1);
        return new CustomResponse();
    }
}
