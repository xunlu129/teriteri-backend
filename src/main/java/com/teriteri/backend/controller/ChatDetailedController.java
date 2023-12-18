package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.service.message.ChatDetailedService;
import com.teriteri.backend.service.utils.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatDetailedController {
    @Autowired
    private ChatDetailedService chatDetailedService;

    @Autowired
    private CurrentUser currentUser;

    @GetMapping("/msg/chat-detailed/get-more")
    public CustomResponse getMoreChatDetails(@RequestParam("uid") Integer uid,
                                             @RequestParam("offset") Long offset) {
        Integer loginUid = currentUser.getUserId();
        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(chatDetailedService.getDetails(uid, loginUid, offset));
        return customResponse;
    }
}
