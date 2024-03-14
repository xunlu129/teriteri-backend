package com.teriteri.backend.service.impl.video;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.teriteri.backend.mapper.FavoriteMapper;
import com.teriteri.backend.mapper.FavoriteVideoMapper;
import com.teriteri.backend.pojo.Favorite;
import com.teriteri.backend.pojo.FavoriteVideo;
import com.teriteri.backend.service.video.FavoriteVideoService;
import com.teriteri.backend.utils.RedisUtil;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FavoriteVideoServiceImpl implements FavoriteVideoService {
    @Autowired
    private FavoriteVideoMapper favoriteVideoMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public Set<Integer> findFidsOfCollected(Integer vid, Set<Integer> fids) {
        if (fids.size() == 0) return new HashSet<>();
        QueryWrapper<FavoriteVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("vid", vid)
                .in("fid", fids)
                .eq("is_remove", 0); // 筛选存在该视频的收藏夹
        List<FavoriteVideo> resultList = favoriteVideoMapper.selectList(queryWrapper);
        // 提取符合条件的 fid 列表，并转换为 Set
        return resultList.stream()
                .map(FavoriteVideo::getFid)
                .collect(Collectors.toSet());
    }

    @Override
    public void addToFav(Integer uid, Integer vid, Set<Integer> fids) {
        Date currentTime = new Date();
        // 使用事务批量操作 减少连接sql的开销
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            // 查询已存在的记录
            QueryWrapper<FavoriteVideo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("vid", vid).in("fid", fids);
            List<FavoriteVideo> existingRecords = favoriteVideoMapper.selectList(queryWrapper);
            // 更新已存在的记录
            UpdateWrapper<FavoriteVideo> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("vid", vid).in("fid", fids).set("time", currentTime).set("is_remove", 0);
            favoriteVideoMapper.update(null, updateWrapper);
            // 插入不存在的记录
            Set<Integer> existingFids = existingRecords.stream().map(FavoriteVideo::getFid).collect(Collectors.toSet());
            Set<Integer> newFids = fids.stream().filter(fid -> !existingFids.contains(fid)).collect(Collectors.toSet());
            List<FavoriteVideo> newRecords = newFids.stream()
                    .map(fid -> new FavoriteVideo(null, vid, fid, currentTime, 0))
                    .collect(Collectors.toList());
            if (!newRecords.isEmpty()) {
                for (FavoriteVideo record : newRecords) {
                    favoriteVideoMapper.insert(record);
                }
            }
            // 更新favorite表的收藏数
            UpdateWrapper<Favorite> updateWrapper1 = new UpdateWrapper<>();
            updateWrapper1.in("fid", fids).setSql("count = count + 1");
            favoriteMapper.update(null, updateWrapper1);
            sqlSession.commit();
        }
        // 更新 Redis 中每个 ZSet
        for (Integer fid : fids) {
            String key = "favorite_video:" + fid;
            redisUtil.zset(key, vid);
        }
        redisUtil.delValue("favorites:" + uid);
    }

    @Override
    public void removeFromFav(Integer uid, Integer vid, Set<Integer> fids) {
        // 使用事务批量操作 减少连接sql的开销
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            UpdateWrapper<FavoriteVideo> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("vid", vid).in("fid", fids).set("is_remove", 1);
            favoriteVideoMapper.update(null, updateWrapper);
            // 更新favorite表的收藏数
            UpdateWrapper<Favorite> updateWrapper1 = new UpdateWrapper<>();
            updateWrapper1.in("fid", fids).setSql("count = CASE WHEN count - 1 < 0 THEN 0 ELSE count - 1 END");
            favoriteMapper.update(null, updateWrapper1);
            sqlSession.commit();
        }
        // 更新 Redis 中每个 ZSet
        for (Integer fid : fids) {
            String key = "favorite_video:" + fid;
            redisUtil.zsetDelMember(key, vid);
        }
        redisUtil.delValue("favorites:" + uid);
    }
}
