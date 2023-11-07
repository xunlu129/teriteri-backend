package com.teriteri.backend.service.impl.video;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.teriteri.backend.mapper.VideoStatsMapper;
import com.teriteri.backend.pojo.VideoStats;
import com.teriteri.backend.service.video.VideoStatsService;
import com.teriteri.backend.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VideoStatsServiceImpl implements VideoStatsService {
    @Autowired
    private VideoStatsMapper videoStatsMapper;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 根据视频ID查询视频常变数据
     * @param vid 视频ID
     * @return
     */
    @Override
    public VideoStats getVideoStatsById(Integer vid) {
        // 多线程查redis反而更慢了，所以干脆直接查数据库
        return videoStatsMapper.selectById(vid);
    }
}
