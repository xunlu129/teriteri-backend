package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.service.video.VideoReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VideoReviewController {

    @Autowired
    private VideoReviewService videoReviewService;

    /**
     * 审核 查询对应状态的视频数量
     * @param status 状态 0待审核 1通过 2未通过
     * @return
     */
    @GetMapping("/review/video/total")
    public CustomResponse getTotal(@RequestParam("vstatus") Integer status) {
        return videoReviewService.getTotalByStatus(status);
    }

    /**
     * 审核 分页查询对应状态视频
     * @param status 状态 0待审核 1通过 2未通过
     * @param page  当前页
     * @param quantity  每页的数量
     * @return
     */
    @GetMapping("/review/video/getpage")
    public CustomResponse getVideos(@RequestParam("vstatus") Integer status,
                                    @RequestParam(value = "page", defaultValue = "1") Integer page,
                                    @RequestParam(value = "quantity", defaultValue = "10") Integer quantity) {
        return videoReviewService.getVideosByPage(status, page, quantity);
    }
}
