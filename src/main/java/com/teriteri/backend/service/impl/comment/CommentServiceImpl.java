package com.teriteri.backend.service.impl.comment;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.teriteri.backend.im.IMServer;
import com.teriteri.backend.mapper.CommentMapper;
import com.teriteri.backend.mapper.VideoMapper;
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
    private VideoMapper videoMapper;

    @Autowired
    private VideoStatsService videoStatsService;

    @Autowired
    private UserService userService;

    @Autowired
    private MsgUnreadService msgUnreadService;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    /**
     * 获取评论树列表
     * @param vid   对应视频ID
     * @param offset 分页偏移量（已经获取到的评论树的数量）
     * @param type  排序类型 1 按热度排序 2 按时间排序
     * @return  评论树列表
     */
    @Override
    public List<CommentTree> getCommentTreeByVid(Integer vid, Long offset, Integer type) {
        // 查询父级评论
        List<Comment> rootComments = getRootCommentsByVid(vid, offset, type);

        // 并行执行每个根级评论的子评论查询任务
        List<CommentTree> commentTreeList = rootComments.stream().parallel()
                .map(rootComment ->buildCommentTree(rootComment, 0L, 2L))
                .collect(Collectors.toList());

//        System.out.println(commentTreeList);

        return commentTreeList;
    }

    /**
     * 构建评论树
     * @param comment 根评论
     * @param start 子评论开始偏移量
     * @param stop  子评论结束偏移量
     * @return  单棵评论树
     */
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

        tree.setUser(userService.getUserById(comment.getUid()));
        tree.setToUser(userService.getUserById(comment.getToUserId()));

        // 递归查询构建子评论树
        // 这里如果是根节点的评论，则查出他的子评论； 如果不是根节点评论，则不查，只填写 User 信息。
        if (comment.getRootId() == 0) {
            long count = redisUtil.zCard("comment_reply:" + comment.getId());
            tree.setCount(count);

            List<Comment> childComments = getChildCommentsByRootId(comment.getId(), comment.getVid(), start, stop);

            List<CommentTree> childTreeList = childComments.stream().parallel()
                            .map(childComment -> buildCommentTree(childComment, start, stop))
                                    .collect(Collectors.toList());

            tree.setReplies(childTreeList);
        }

        return tree;
    }

    /**
     * 发送评论，字数不得大于2000或为空
     * @param vid   视频id
     * @param uid   发布者uid
     * @param rootId    楼层id（根评论id）
     * @param parentId  被回复的评论id
     * @param toUserId  被回复用户uid
     * @param content   评论内容
     * @return  true 发送成功 false 发送失败
     */
    @Override
    @Transactional
    public CommentTree sendComment(Integer vid, Integer uid, Integer rootId, Integer parentId, Integer toUserId, String content) {
        if (content == null || content.length() == 0 || content.length() > 2000) return null;
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
        // 更新视频评论 + 1
        videoStatsService.updateStats(comment.getVid(), "comment", true, 1);

        CommentTree commentTree = buildCommentTree(comment, 0L, -1L);

        try {
            CompletableFuture.runAsync(() -> {
                // 如果不是根级评论，则加入 redis 对应的 zset 中
                if (!rootId.equals(0)) {
                    redisUtil.zset("comment_reply:" + rootId, comment.getId());
                } else {
                    redisUtil.zset("comment_video:"+ vid, comment.getId());
                }
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
        } catch (Exception e) {
            log.error("发送评论过程中出现一点差错");
            e.printStackTrace();
        }

        return commentTree;
    }

    /**
     * 删除评论
     * @param id    评论id
     * @param uid   当前用户id
     * @param isAdmin   是否是管理员
     * @return  响应对象
     */
    @Override
    @Transactional
    public CustomResponse deleteComment(Integer id, Integer uid, boolean isAdmin) {
        CustomResponse customResponse = new CustomResponse();
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id).ne("is_deleted", 1);
        Comment comment = commentMapper.selectOne(queryWrapper);
        if (comment == null) {
            customResponse.setCode(404);
            customResponse.setMessage("评论不存在");
            return customResponse;
        }

        // 判断该用户是否有权限删除这条评论
        Video video = videoMapper.selectById(comment.getVid());
        if (Objects.equals(comment.getUid(), uid) || isAdmin || Objects.equals(video.getUid(), uid)) {
            // 删除评论
            UpdateWrapper<Comment> commentWrapper = new UpdateWrapper<>();
            commentWrapper.eq("id", comment.getId()).set("is_deleted", 1);
            commentMapper.update(null, commentWrapper);

            /*
             如果该评论是根节点评论，则删掉其所有回复。
             如果不是根节点评论，则将他所在的 comment_reply(zset) 中的 comment_id 删掉
             */
            if (Objects.equals(comment.getRootId(), 0)) {
                // 查询总共要减少多少评论数
                int count = Math.toIntExact(redisUtil.zCard("comment_reply:" + comment.getId()));
                videoStatsService.updateStats(comment.getVid(), "comment", false, count + 1);
                redisUtil.zsetDelMember("comment_video:" + comment.getVid(), comment.getId());
                redisUtil.delValue("comment_reply:" + comment.getId());
            } else {
                videoStatsService.updateStats(comment.getVid(), "comment", false, 1);
                redisUtil.zsetDelMember("comment_reply:" + comment.getRootId(), comment.getId());
            }

            customResponse.setCode(200);
            customResponse.setMessage("删除成功!");
        } else {
            customResponse.setCode(403);
            customResponse.setMessage("你无权删除该条评论");
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
        Set<Object> replyIds = redisUtil.zRange("comment_reply:" + rootId, start, stop);

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
        Set<Object> rootIdsSet;
        if (type == 1) {
            // 按热度排序就不能用时间分数查偏移量了，要全部查出来，后续在MySQL筛选
            rootIdsSet = redisUtil.zReverange("comment_video:" + vid, 0L, -1L);
        } else {
            rootIdsSet = redisUtil.zReverange("comment_video:" + vid, offset, offset + 9L);
        }

        if (rootIdsSet == null || rootIdsSet.isEmpty()) return Collections.emptyList();

        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.in("id", rootIdsSet).ne("is_deleted", 1);
        if (type == 1) { // 热度
            wrapper.orderByDesc("(love - bad)").last("LIMIT 10 OFFSET " + offset);
        } else { // 时间
            wrapper.orderByDesc("create_time");
        }
        return commentMapper.selectList(wrapper);
    }

    /**
     * 获取更多回复评论
     * @param id 根评论id
     * @return  包含全部回复评论的评论树
     */
    @Override
    public CommentTree getMoreCommentsById(Integer id) {
        Comment comment = commentMapper.selectById(id);
        return buildCommentTree(comment, 0L, -1L);
    }

    /**
     * 同时相对更新点赞和点踩
     * @param id    评论id
     * @param addLike   true 点赞 false 点踩
     */
    @Override
    public void updateLikeAndDisLike(Integer id, boolean addLike) {
        UpdateWrapper<Comment> updateWrapper = new UpdateWrapper<>();
        if (addLike) {
            updateWrapper.setSql("love = love + 1, bad = CASE WHEN " +
                    "bad - 1 < 0 " +
                            "THEN 0 " +
                            "ELSE bad - 1 END");
        } else {
            updateWrapper.setSql("bad = bad + 1, love = CASE WHEN " +
                    "love - 1 < 0 " +
                    "THEN 0 " +
                    "ELSE love - 1 END");
        }

        commentMapper.update(null, updateWrapper);
    }

    /**
     * 单独更新点赞或点踩
     * @param id    评论id
     * @param column    "love" 点赞 "bad" 点踩
     * @param increase  true 增加 false 减少
     * @param count     更改数量
     */
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