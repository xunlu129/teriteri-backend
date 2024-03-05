package com.teriteri.backend.service.impl.video;

import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.teriteri.backend.mapper.FavoriteMapper;
import com.teriteri.backend.pojo.Favorite;
import com.teriteri.backend.service.video.FavoriteService;
import com.teriteri.backend.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
public class FavoriteServiceImpl implements FavoriteService {
    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<Favorite> getFavorites(Integer uid, boolean isOwner) {
        String key = "favorites:" + uid;   // uid用户的收藏夹列表
        String string = redisUtil.getObjectString(key);
        List<Favorite> list = JSONArray.parseArray(string, Favorite.class);
        if (list != null) {
            if (!isOwner) {
                List<Favorite> list1 = new ArrayList<>();
                for (Favorite favorite : list) {
                    if (favorite.getVisible() == 1) {
                        list1.add(favorite);
                    }
                }
                return list1;
            }
            return list;
        }
        QueryWrapper<Favorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid).ne("is_delete", 1).orderByDesc("fid");
        list = favoriteMapper.selectList(queryWrapper);
        if (list != null && !list.isEmpty()) {
            List<Favorite> finalList = list;
            CompletableFuture.runAsync(() -> {
                redisUtil.setExObjectValue(key, finalList);
            });
            if (!isOwner) {
                List<Favorite> list1 = new ArrayList<>();
                for (Favorite favorite : list) {
                    if (favorite.getVisible() == 1) {
                        list1.add(favorite);
                    }
                }
                return list1;
            }
            return list;
        }
        return null;
    }

    @Override
    public Favorite addFavorite(Integer uid, String title, String desc, Integer visible) {
        // 懒得做字数等的合法判断了，前端做吧
        Favorite favorite = new Favorite(null, uid, 2, visible, null, title, desc, 0, null);
        favoriteMapper.insert(favorite);
        redisUtil.delValue("favorites:" + uid);
        return favorite;
    }

    @Override
    public Favorite updateFavorite(Integer fid, Integer uid, String title, String desc, Integer visible) {
        Favorite favorite = favoriteMapper.selectById(fid);
        if (!Objects.equals(favorite.getUid(), uid)) {
            return null;
        }
        UpdateWrapper<Favorite> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("fid", fid).set("title", title).set("description", desc).set("visible", visible);
        favoriteMapper.update(null, updateWrapper);
        redisUtil.delValue("favorites:" + uid);
        return favorite;
    }

    @Override
    public void delFavorite(Integer fid, Integer uid) {

    }
}
