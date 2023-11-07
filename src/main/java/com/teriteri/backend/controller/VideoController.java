package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.service.video.VideoService;
import com.teriteri.backend.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class VideoController {
    @Autowired
    private VideoService videoService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 更新视频状态，包括过审、不通过、删除，其中审核相关需要管理员权限，删除可以是管理员或者投稿用户
     * @param vid 视频ID
     * @param status 要修改的状态，1通过 2不通过 3删除
     * @return 无data返回 仅返回响应
     */
    @PostMapping("/video/change/status")
    public CustomResponse updateStatus(@RequestParam("vid") Integer vid,
                                       @RequestParam("status") Integer status) {
        return videoService.updateVideoStatus(vid, status);
    }

    @GetMapping("/video/random/visitor")
    public CustomResponse randomVideosForVisitor() {
        CustomResponse customResponse = new CustomResponse();
        int count = 11;
        Set<Object> idSet = redisUtil.srandmember("video_status:1", count);
        List<Map<String, Object>> videoList = videoService.getVideosWithDataByIds(idSet, 1, count);
        // 随机打乱列表顺序
        Collections.shuffle(videoList);
        customResponse.setData(videoList);
        return customResponse;
    }
}
