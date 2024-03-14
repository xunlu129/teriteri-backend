package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.service.message.MsgUnreadService;
import com.teriteri.backend.service.utils.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    /**
     * 清除某一列的未读消息提示
     * @param column    msg_unread表列名 "reply"/"at"/"love"/"system"/"whisper"/"dynamic"
     */
    @PostMapping("/msg-unread/clear")
    public void clearUnread(@RequestParam("column") String column) {
        Integer uid = currentUser.getUserId();
        msgUnreadService.clearUnread(uid, column);
    }
}
