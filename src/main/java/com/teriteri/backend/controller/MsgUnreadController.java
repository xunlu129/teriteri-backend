package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.service.message.MsgUnreadService;
import com.teriteri.backend.service.utils.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MsgUnreadController {
    @Autowired
    private MsgUnreadService msgUnreadService;

    @Autowired
    private CurrentUser currentUser;

    /**
     * 获取当前用户全部消息未读数
     * @return
     */
    @GetMapping("/msg-unread/all")
    public CustomResponse getMsgUnread() {
        Integer uid = currentUser.getUserId();
        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(msgUnreadService.getUnread(uid));
        return customResponse;
    }
}
