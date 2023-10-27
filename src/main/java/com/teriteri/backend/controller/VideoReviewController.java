package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.service.video.VideoReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VideoReviewController {

    @Autowired
    private VideoReviewService videoReviewService;

    @GetMapping("/review/video/getall")
    public CustomResponse getUnderReviewVideos() {
        return videoReviewService.getUnderReviewVideos();
    }
}
