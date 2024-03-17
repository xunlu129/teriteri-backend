package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CommentTree;
import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.service.comment.CommentService;
import com.teriteri.backend.service.utils.CurrentUser;
import com.teriteri.backend.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class CommentController {
    @Autowired
    private CommentService commentService;
    @Autowired
    private CurrentUser currentUser;
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 获取评论树列表，每次查十条
     * @param vid   对应视频ID
     * @param offset 分页偏移量（已经获取到的评论树的数量）
     * @param type  排序类型 1 按热度排序 2 按时间排序
     * @return  评论树列表
     */
    @GetMapping("/comment/get")
    public CustomResponse getCommentTreeByVid(@RequestParam("vid") Integer vid,
                                                 @RequestParam("offset") Long offset,
                                                 @RequestParam("type") Integer type) {
        CustomResponse customResponse = new CustomResponse();
        long count = redisUtil.zCard("comment_video:" + vid);
        Map<String, Object> map = new HashMap<>();
        if (offset >= count) {
            // 表示前端已经获取到全部根评论了，没必要继续
            map.put("more", false);
            map.put("comments", Collections.emptyList());
        } else if (offset + 10 >= count){
            // 表示这次查询会查完全部根评论
            map.put("more", false);
            map.put("comments", commentService.getCommentTreeByVid(vid, offset, type));
        } else {
            // 表示这次查的只是冰山一角，还有很多评论没查到
            map.put("more", true);
            map.put("comments", commentService.getCommentTreeByVid(vid, offset, type));
        }
        customResponse.setData(map);
        return customResponse;
    }

    /**
     * 展开更多回复评论
     * @param id 根评论id
     * @return 完整的一棵包含全部评论的评论树
     */
    @GetMapping("/comment/reply/get-more")
    public CommentTree getMoreCommentById(@RequestParam("id") Integer id) {
        return commentService.getMoreCommentsById(id);
    }

    /**
     * 发表评论
     * @param vid   视频id
     * @param rootId    根评论id
     * @param parentId  被回复评论id
     * @param toUserId  被回复者uid
     * @param content   评论内容
     * @return  响应对象
     */
    @PostMapping("/comment/add")
    public CustomResponse addComment(
            @RequestParam("vid") Integer vid,
            @RequestParam("root_id") Integer rootId,
            @RequestParam("parent_id") Integer parentId,
            @RequestParam("to_user_id") Integer toUserId,
            @RequestParam("content") String content ) {
        Integer uid = currentUser.getUserId();

        CustomResponse customResponse = new CustomResponse();
        CommentTree commentTree = commentService.sendComment(vid, uid, rootId, parentId, toUserId, content);
        if (commentTree == null) {
            customResponse.setCode(500);
            customResponse.setMessage("发送失败！");
        }
        customResponse.setData(commentTree);
        return customResponse;
    }

    /**
     * 删除评论
     * @param id 评论id
     * @return  响应对象
     */
    @PostMapping("/comment/delete")
    public CustomResponse delComment(@RequestParam("id") Integer id) {
        Integer loginUid = currentUser.getUserId();
        return commentService.deleteComment(id, loginUid, currentUser.isAdmin());
    }
}
