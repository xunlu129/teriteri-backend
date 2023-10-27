package com.teriteri.backend.service.impl.video;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.teriteri.backend.mapper.VideoMapper;
import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.Video;
import com.teriteri.backend.service.utils.CurrentUser;
import com.teriteri.backend.service.video.VideoReviewService;
import com.teriteri.backend.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class VideoReviewServiceImpl implements VideoReviewService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private CurrentUser currentUser;

    /**
     * 获取全部待审核的视频
     * @return CustomResponse对象，包含待审核的视频列表
     */
    @Override
    public CustomResponse getUnderReviewVideos() {
        CustomResponse customResponse = new CustomResponse();
        if (!currentUser.isAdmin()) {
            customResponse.setCode(403);
            customResponse.setMessage("您不是管理员，无权访问");
            return customResponse;
        }
        // 从 redis 获取待审核的视频id集合，为了提升效率就不遍历数据库了，前提得保证 Redis 没崩，数据一致性采用定时同步或者中间件来保证
        Set<Object> set = redisUtil.getMembers("video_status:0");
        if (set != null && set.size() != 0) {
            // 如果集合不空，则在数据库主键查询，并且返回没有被删除的视频
            QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("vid", set);
            queryWrapper.isNull("delete_date");
            List<Video> videoList = videoMapper.selectList(queryWrapper);
            customResponse.setData(videoList);
        }
        return customResponse;
    }
}
