package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.Comment;
import com.teriteri.backend.pojo.CommentTree;
import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.service.comment.CommentService;
import com.teriteri.backend.service.utils.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
public class CommentController {
    @Autowired
    private CommentService commentService;
    @Autowired
    private CurrentUser currentUser;

    @GetMapping("/comment/get")
    public List<CommentTree> getCommentTreeByVid(@RequestParam("vid") Integer vid,
                                                 @RequestParam("offset") Long offset,
                                                 @RequestParam("type") Integer type) {
        return commentService.getCommentTreeByVid(vid, offset, type);
    }

    @GetMapping("/comment/reply/get-more")
    public CommentTree getMoreCommentById(@RequestParam("id") Integer id) {
        return commentService.getMoreCommentsById(id);
    }

    @PostMapping("/comment/add")
    public CustomResponse addComment(
            @RequestParam("vid") Integer vid,
            @RequestParam("root_id") Integer rootId,
            @RequestParam("parent_id") Integer parentId,
            @RequestParam("to_user_id") Integer toUserId,
            @RequestParam("content") String content ) {
        System.out.println("接收到:" + " " + vid + " " + rootId + " " + parentId + " " + toUserId + " " + content);
        Integer uid = currentUser.getUserId();
        System.out.println("uid:" + uid);

        CustomResponse customResponse = new CustomResponse();
        boolean isSucceed = commentService.sendComment(vid, uid, rootId, parentId, toUserId, content);
        if (!isSucceed) {
            customResponse.setCode(500);
            customResponse.setMessage("发送失败！");
        }
        return customResponse;
    }

    @PostMapping("/comment/delete")
    public CustomResponse delComment(@RequestParam("id") Integer id) {
        Integer loginUid = currentUser.getUserId();
        return commentService.deleteComment(id, loginUid);
    }

    @PostMapping("/comment/update")
    public CustomResponse updateComment(@RequestBody Map<String, String> map) {
        Integer id = Integer.valueOf(map.get("id"));
        Integer uid = Integer.valueOf(map.get("uid"));
        String content = map.get("content");
        return commentService.updateComment(id, uid, content);
    }
}
