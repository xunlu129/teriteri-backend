package com.teriteri.backend.service.impl.video;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.teriteri.backend.mapper.VideoStatsMapper;
import com.teriteri.backend.pojo.VideoStats;
import com.teriteri.backend.service.video.VideoStatsService;
import com.teriteri.backend.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
public class VideoStatsServiceImpl implements VideoStatsService {
    @Autowired
    private VideoStatsMapper videoStatsMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    /**
     * 根据视频ID查询视频常变数据
     * @param vid 视频ID
     * @return 视频数据统计
     */
    @Override
    public VideoStats getVideoStatsById(Integer vid) {
        VideoStats videoStats = redisUtil.getObject("videoStats:" + vid, VideoStats.class);
        if (videoStats == null) {
            videoStats = videoStatsMapper.selectById(vid);
            if (videoStats != null) {
                VideoStats finalVideoStats = videoStats;
                CompletableFuture.runAsync(() -> {
                    redisUtil.setExObjectValue("videoStats:" + vid, finalVideoStats);    // 异步更新到redis
                }, taskExecutor);
            } else {
                return null;
            }
        }
        // 多线程查redis反而更慢了，所以干脆直接查数据库
        return videoStats;
    }

    /**
     * 更新指定字段
     * @param vid   视频ID
     * @param column    对应数据库的列名
     * @param increase  是否增加，true则增加 false则减少
     * @param count 增减数量 一般是1，只有投币可以加2
     */
    @Override
    public void updateStats(Integer vid, String column, boolean increase, Integer count) {
        UpdateWrapper<VideoStats> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("vid", vid);
        if (increase) {
            updateWrapper.setSql(column + " = " + column + " + " + count);
        } else {
            // 更新后的字段不能小于0
            updateWrapper.setSql(column + " = CASE WHEN " + column + " - " + count + " < 0 THEN 0 ELSE " + column + " - " + count + " END");
        }
        videoStatsMapper.update(null, updateWrapper);
        redisUtil.delValue("videoStats:" + vid);
    }

    /**
     * 同时更新点赞和点踩
     * @param vid   视频ID
     * @param addGood   是否点赞，true则good+1&bad-1，false则good-1&bad+1
     */
    @Override
    public void updateGoodAndBad(Integer vid, boolean addGood) {
        UpdateWrapper<VideoStats> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("vid", vid);
        if (addGood) {
            updateWrapper.setSql("good = good + 1");
            updateWrapper.setSql("bad = CASE WHEN bad - 1 < 0 THEN 0 ELSE bad - 1 END");
        } else {
            updateWrapper.setSql("bad = bad + 1");
            updateWrapper.setSql("good = CASE WHEN good - 1 < 0 THEN 0 ELSE good - 1 END");
        }
        videoStatsMapper.update(null, updateWrapper);
        redisUtil.delValue("videoStats:" + vid);
    }
}
