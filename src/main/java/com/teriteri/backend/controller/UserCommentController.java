package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.service.comment.UserCommentService;
import com.teriteri.backend.service.utils.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
public class UserCommentController {
    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private UserCommentService userCommentService;

    /**
     * 获取用户点赞点踩评论集合
     */
    @GetMapping("/comment/get-like-and-dislike")
    public CustomResponse getLikeAndDislike() {
        Integer uid = currentUser.getUserId();

        CustomResponse response = new CustomResponse();
        response.setCode(200);
        response.setData(userCommentService.getUserLikeAndDislike(uid));

        return response;
    }

    /**
     * 点赞或点踩某条评论
     * @param id    评论id
     * @param isLike true 赞 false 踩
     * @param isSet  true 点 false 取消
     */
    @PostMapping("/comment/love-or-not")
    public CustomResponse loveOrNot(@RequestParam("id") Integer id,
                          @RequestParam("isLike") boolean isLike,
                          @RequestParam("isSet") boolean isSet) {
        Integer uid = currentUser.getUserId();
        userCommentService.userSetLikeOrUnlike(uid, id, isLike, isSet);
        return new CustomResponse();
    }

    /**
     * 获取UP主觉得很淦的评论
     * @param uid   UP主uid
     * @return  点赞的评论id列表
     */
    @GetMapping("/comment/get-up-like")
    public CustomResponse getUpLike(@RequestParam("uid") Integer uid) {
        CustomResponse customResponse = new CustomResponse();
        Map<String, Object> map = userCommentService.getUserLikeAndDislike(uid);
        customResponse.setData(map.get("userLike"));
        return customResponse;
    }
}
