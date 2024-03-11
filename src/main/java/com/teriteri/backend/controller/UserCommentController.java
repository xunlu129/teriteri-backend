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

@Slf4j
@RestController
public class UserCommentController {
    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private UserCommentService userCommentService;

    @GetMapping("/comment/get-like-and-dislike")
    public CustomResponse getLikeAndDislike() {
        Integer uid = currentUser.getUserId();

        CustomResponse response = new CustomResponse();
        response.setCode(200);
        response.setData(userCommentService.getUserLikeAndDislike(uid));

        return response;
    }

    @PostMapping("/comment/love-or-not")
    public void loveOrNot(@RequestParam("id") Integer id,
                          @RequestParam("isLike") boolean isLike,
                          @RequestParam("isSet") boolean isSet) {
        Integer uid = currentUser.getUserId();
        userCommentService.userSetLikeOrUnlike(uid, id, isLike, isSet);
    }
}
