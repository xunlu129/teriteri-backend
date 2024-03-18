package com.teriteri.backend.service.impl.comment;

import com.teriteri.backend.service.comment.CommentService;
import com.teriteri.backend.service.comment.UserCommentService;
import com.teriteri.backend.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
public class UserCommentServiceImpl implements UserCommentService {
    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    @Autowired
    private CommentService commentService;

    /**
     * 获取用户点赞和点踩的评论集合
     * @param uid   当前用户
     * @return  点赞和点踩的评论集合
     */
    @Override
    public Map<String, Object> getUserLikeAndDislike(Integer uid) {
        Map<String, Object> map = new HashMap<>();
        // 获取用户点赞列表，并放入map中
        CompletableFuture<Void> userLikeFuture = CompletableFuture.runAsync(() -> {
            Object userLike = redisUtil.getMembers("user_like_comment:" + uid);
            if (userLike == null) {
                map.put("userLike", Collections.emptySet());
            } else{
                map.put("userLike", userLike);
            }
        }, taskExecutor);
        // 获取也用户点踩列表，并放入map中
        CompletableFuture<Void> userDislikeFuture = CompletableFuture.runAsync(() -> {
            Object userDislike = redisUtil.getMembers("user_dislike_comment:" + uid);
            map.put("userDislike", userDislike);
            if (userDislike == null) {
                map.put("userDislike", Collections.emptySet());
            } else {
                map.put("userDislike", userDislike);
            }
        }, taskExecutor);

        userDislikeFuture.join();
        userLikeFuture.join();

        return map;
    }

    /**
     * 点赞或点踩某条评论
     * @param uid   当前用户id
     * @param id    评论id
     * @param isLike true 赞 false 踩
     * @param isSet true 点 false 取消
     */
    @Override
    public void userSetLikeOrUnlike(Integer uid, Integer id, boolean isLike, boolean isSet) {
        Boolean likeExist = redisUtil.isMember("user_like_comment:" + uid, id);
        Boolean dislikeExist = redisUtil.isMember("user_dislike_comment:" + uid, id);

        // 点赞
        if (isLike && isSet) {
            // 原本就点了赞
            if (likeExist) {
                return;
            }
            // 原本点了踩，就要取消踩
            if (dislikeExist) {
                // 1.redis中删除点踩记录
                CompletableFuture.runAsync(() -> {
                    redisUtil.delMember("user_dislike_comment:" + uid, id);
                }, taskExecutor);
                // 2. 数据库中更改评论的点赞点踩数
                CompletableFuture.runAsync(() -> {
                    commentService.updateLikeAndDisLike(id, true);
                }, taskExecutor);

            } else {
                // 原来没点踩，只需要点赞, 这里只更新评论的点赞数，下面添加点赞记录
                CompletableFuture.runAsync(() -> {
                    commentService.updateComment(id, "love", true, 1);
                }, taskExecutor);
            }
            // 添加点赞记录
            redisUtil.addMember("user_like_comment:" + uid, id);
        } else if (isLike) {
            // 取消点赞
            if (!likeExist) {
                // 原本就没有点赞，直接返回
                return;
            }
            CompletableFuture.runAsync(() -> {
                // 移除点赞记录
                redisUtil.delMember("user_like_comment:" + uid, id);
            }, taskExecutor);
            // 更新评论点赞数
            CompletableFuture.runAsync(() -> {
                commentService.updateComment(id, "love", false, 1);
            }, taskExecutor);
        } else if (isSet) {
            // 点踩
            if (dislikeExist) {
                // 原本就点了踩，直接返回
                return;
            }

            if (likeExist) {
                // 原本点了赞，要取消赞
                CompletableFuture.runAsync(() -> {
                    redisUtil.delMember("user_like_comment:" + uid, id);
                }, taskExecutor);
                // 更新评论点赞点踩的记录
                CompletableFuture.runAsync(() -> {
                    commentService.updateLikeAndDisLike(id, false);
                }, taskExecutor);

            } else {
                // 原本没有点赞，直接点踩，更新评论点踩数量
                CompletableFuture.runAsync(() -> {
                    commentService.updateComment(id, "bad", true, 1);
                }, taskExecutor);
            }
            // 更新用户点踩记录
            redisUtil.addMember("user_dislike_comment:" + uid, id);
        } else {
            // 取消点踩
            if (!dislikeExist) {
                // 原本就没有点踩直接返回
                return;
            }

            CompletableFuture.runAsync(() -> {
                // 取消用户点踩记录
                redisUtil.delMember("user_dislike_comment:" + uid, id);
            }, taskExecutor);

            CompletableFuture.runAsync(() -> {
                // 更新评论点踩数量
                commentService.updateComment(id, "bad", false, 1);
            }, taskExecutor);
        }
    }
}
