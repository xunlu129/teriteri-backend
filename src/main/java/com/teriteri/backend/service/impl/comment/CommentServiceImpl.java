package com.teriteri.backend.service.impl.comment;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.teriteri.backend.im.IMServer;
import com.teriteri.backend.mapper.CommentMapper;
import com.teriteri.backend.pojo.*;
import com.teriteri.backend.service.comment.CommentService;
import com.teriteri.backend.service.message.MsgUnreadService;
import com.teriteri.backend.service.user.UserService;
import com.teriteri.backend.service.video.VideoStatsService;
import com.teriteri.backend.utils.RedisUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;


@Slf4j
@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private VideoStatsService videoStatsService;

    @Autowired
    private UserService userService;

    @Autowired
    private MsgUnreadService msgUnreadService;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;


    @Override
    public List<CommentTree> getCommentTreeByVid(Integer vid, Long offset, Integer type) {
        // 查询父级评论
        List<Comment> rootComments = getRootCommentsByVid(vid, offset, type);

        // 异步执行每个根级评论的子评论查询任务
        List<CompletableFuture<CommentTree>> futureList = rootComments.stream()
                .map(rootComment ->
                        CompletableFuture.supplyAsync(
                                () -> buildCommentTree(rootComment, 0L, 2L),
                                taskExecutor))
                .collect(Collectors.toList());

        // 等待所有异步任务执行完成
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));

        // 获取评论树
        List<CommentTree> commentTreeList = allOf.thenApplyAsync(v -> futureList.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList())).join();

//        System.out.println(commentTreeList);

        return commentTreeList;
    }

    private CommentTree buildCommentTree(Comment comment, Long start, Long stop) {
        CommentTree tree = new CommentTree();
        tree.setId(comment.getId());
        tree.setVid(comment.getVid());
        tree.setRootId(comment.getRootId());
        tree.setParentId(comment.getParentId());
        tree.setContent(comment.getContent());
        tree.setCreateTime(comment.getCreateTime());
        tree.setLove(comment.getLove());
        tree.setBad(comment.getBad());

        CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
            tree.setUser(userService.getUserById(comment.getUid()));
        }, taskExecutor);

        CompletableFuture<Void> toUserFuture = CompletableFuture.runAsync(() -> {
            tree.setToUser(userService.getUserById(comment.getToUserId()));
        }, taskExecutor);

        userFuture.join();
        toUserFuture.join();

        // 递归查询构建子评论树
        // 这里如果是根节点的评论，则查出他的子评论； 如果不是根节点评论，则不查，只填写 User 信息。
        if (comment.getRootId() == 0) {
            long count = redisUtil.zCount("comment_reply:" + comment.getId(), 0L, Long.MAX_VALUE);
            tree.setCount(count);

            List<Comment> childComments = getChildCommentsByRootId(comment.getId(), comment.getVid(), start, stop);

            List<CompletableFuture<CommentTree>> futureList = new ArrayList<>();
            for (Comment childComment : childComments) {
                CompletableFuture<CommentTree> future = CompletableFuture.supplyAsync(
                        () -> buildCommentTree(childComment, start, stop),
                        taskExecutor);
                futureList.add(future);
            }

            CompletableFuture<Void> allOf = CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
            allOf.join();

            List<CommentTree> childTreeList = futureList.stream().map(CompletableFuture::join).collect(Collectors.toList());
            tree.setReplies(childTreeList);
        }

        return tree;
    }

    @Override
    @Transactional
    public boolean sendComment(Integer vid, Integer uid, Integer rootId, Integer parentId, Integer toUserId, String content) {
        try {
            Comment comment = new Comment(
                    null,
                    vid,
                    uid,
                    rootId,
                    parentId,
                    toUserId,
                    content,
                    0,
                    0,
                    new Date(),
                    null,
                    null
            );
            commentMapper.insert(comment);

            CompletableFuture<Void> videoStatsFuture = CompletableFuture.runAsync(() -> {
                // 更新视频评论 + 1
                videoStatsService.updateStats(comment.getVid(), "comment", true, 1);
            }, taskExecutor);

            CompletableFuture<Void> redisFuture = CompletableFuture.runAsync(() -> {
                // 如果不是根级评论，则加入 redis 对应的 zset 中
                if (!rootId.equals(0)) {
                    redisUtil.zset("comment_reply:" + rootId, comment.getId());
                } else {
                    redisUtil.zset("comment_video:"+ vid, comment.getId());
                }
            }, taskExecutor);

            CompletableFuture<Void> nettyFuture = CompletableFuture.runAsync(() -> {
                // 表示被回复的用户收到的回复评论的 id 有序集合
                // 如果不是回复自己
                if(!Objects.equals(comment.getToUserId(), comment.getUid())) {
                    redisUtil.zset("reply_zset:" + comment.getToUserId(), comment.getId());
                    msgUnreadService.addOneUnread(comment.getToUserId(), "reply");

                    // netty 通知未读消息
                    Map<String, Object> map = new HashMap<>();
                    map.put("type", "接收");
                    Set<Channel> myChannels = IMServer.userChannel.get(comment.getToUserId());
                    if (myChannels != null) {
                        for (Channel channel: myChannels) {
                            channel.writeAndFlush(IMResponse.message("reply", map));
                        }
                    }
                }
            }, taskExecutor);

            videoStatsFuture.join();
            redisFuture.join();
            nettyFuture.join();

            return true;
        } catch (Exception e) {
            log.error("Failed to send comment: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public CustomResponse deleteComment(Integer id, Integer uid) {
        CustomResponse customResponse = new CustomResponse();
        try {
            Comment comment = commentMapper.selectById(id);
            if (comment == null) {
                customResponse.setCode(404);
                customResponse.setMessage("该条评论不存在!");
                return customResponse;
            }

            // 判断该用户能否删除这条评论
            if (Objects.equals(comment.getUid(), uid)) {
                // 删除评论
                UpdateWrapper<Comment> commentWrapper = new UpdateWrapper<>();
                commentWrapper.eq("id", comment.getId()).set("is_deleted", 1);
                commentMapper.update(null, commentWrapper);

                /*
                 如果该评论是根节点评论，则删掉其所有回复。
                 如果不是根节点评论，则将他所在的 comment_reply(zset) 中的 comment_id 删掉
                 */
                if (Objects.equals(comment.getRootId(), 0)) {
                    redisUtil.zsetDelMember("comment_video:" + comment.getVid(), comment.getId());
                } else {
                    redisUtil.zsetDelMember("comment_reply:" + comment.getRootId(), comment.getId());
                }

                // 视频回复数量 - 1
                videoStatsService.updateStats(comment.getVid(), "comment", false, 1);

                customResponse.setCode(200);
                customResponse.setMessage("删除成功!");
            } else {
                customResponse.setCode(500);
                customResponse.setMessage("你无权删除该条评论");
            }
        } catch (Exception e) {
            log.error("Failed to delete comment: " + e.getMessage(), e);
            customResponse.setCode(500);
            customResponse.setMessage("删除评论失败");
        }
        return customResponse;
    }

    @Override
    public CustomResponse updateComment(Integer id, Integer uid, String content) {
        Comment comment = commentMapper.selectById(id);
        CustomResponse customResponse = new CustomResponse();
        if (comment == null) {
            customResponse.setCode(500);
            customResponse.setMessage("该条评论不存在");
            return customResponse;
        }

        if (Objects.equals(comment.getUid(), uid)) {
            comment.setContent(content);
            commentMapper.updateById(comment);

            customResponse.setCode(200);
            customResponse.setMessage("修改成功");
        } else {
            customResponse.setCode(500);
            customResponse.setMessage("你无权修改此评论");
        }
        return customResponse;
    }

    /**
     * @param rootId 根级节点的评论 id, 即楼层 id
     * @param vid 视频的 vid
     * @return 1. 根据 redis 查找出回复该评论的子评论 id 列表
     * 2. 根据 id 多线程查询出所有评论的详细信息
     */
    @Override
    public List<Comment> getChildCommentsByRootId(Integer rootId, Integer vid, Long start, Long stop) {
        Set<Object> replyIds = redisUtil.zReverange("comment_reply:" + rootId, start, stop);

        if (replyIds == null || replyIds.isEmpty()) return Collections.emptyList();

        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.in("id", replyIds).ne("is_deleted", 1);

        return commentMapper.selectList(wrapper);
    }

    /**
     * 根据视频 vid 获取根评论列表，一次查 10 条
     * @param vid 视频 id
     * @param offset 偏移量，已经获取到的根评论数量
     * @param type 1:按热度排序 2:按时间排序
     * @return List<Comment>
     */
    @Override
    public List<Comment> getRootCommentsByVid(Integer vid, Long offset, Integer type) {
        Set<Object> rootIdsSet = redisUtil.zReverange("comment_video:" + vid, offset, offset + 9L);

        if (rootIdsSet == null || rootIdsSet.isEmpty()) return Collections.emptyList();

        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.in("id", rootIdsSet).ne("is_deleted", 1);
        if (type == 1) { // 热度
            wrapper.orderByDesc("love");
        } else if (type == 2) { // 时间
            wrapper.orderByDesc("create_time");
        }

        return commentMapper.selectList(wrapper);
    }

    @Override
    public CommentTree getMoreCommentsById(Integer id) {
        Comment comment = commentMapper.selectById(id);
        return buildCommentTree(comment, 0L, -1L);
    }

    @Override
    public void updateLikeAndDisLike(Integer id, boolean addLike) {
        UpdateWrapper<Comment> updateWrapper = new UpdateWrapper<>();
        if (addLike) {
            updateWrapper.setSql("love = love + 1, bad = CASE WHEN " +
                    "bad - 1 < 0 " +
                            "THEN 0 " +
                            "ELSE bad - 1 ");
        } else {
            updateWrapper.setSql("bad = bad + 1, love = CASE WHEN " +
                    "love - 1 < 0 " +
                    "THEN 0 " +
                    "ELSE love - 1 ");
        }

        commentMapper.update(null, updateWrapper);
    }

    @Override
    public void updateComment(Integer id, String column, boolean increase, Integer count) {
        UpdateWrapper<Comment> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        if (increase) {
            updateWrapper.setSql(column + " = " + column + " + " + count);
        } else {
            // 更新后的字段不能小于0
            updateWrapper.setSql(column + " = CASE WHEN " + column + " - " + count + " < 0 THEN 0 ELSE " + column + " - " + count + " END");
        }
        commentMapper.update(null, updateWrapper);
    }
}