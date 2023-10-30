package com.teriteri.backend.service.video;

import com.teriteri.backend.pojo.CustomResponse;

public interface VideoReviewService {
    CustomResponse getTotalByStatus(Integer status);

    CustomResponse getVideosByPage(Integer status, Integer page, Integer quantity);
}
