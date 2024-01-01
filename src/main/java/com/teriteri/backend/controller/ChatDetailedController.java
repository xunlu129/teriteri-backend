package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.service.message.ChatDetailedService;
import com.teriteri.backend.service.utils.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatDetailedController {
    @Autowired
    private ChatDetailedService chatDetailedService;

    @Autowired
    private CurrentUser currentUser;

    /**
     * 获取更多历史消息记录
     * @param uid   聊天对象的UID
     * @param offset    偏移量，即已经获取过的消息数量，从哪条开始获取更多
     * @return  CustomResponse对象，包含更多消息记录的map
     */
    @GetMapping("/msg/chat-detailed/get-more")
    public CustomResponse getMoreChatDetails(@RequestParam("uid") Integer uid,
                                             @RequestParam("offset") Long offset) {
        Integer loginUid = currentUser.getUserId();
        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(chatDetailedService.getDetails(uid, loginUid, offset));
        return customResponse;
    }

    /**
     * 删除消息
     * @param id    消息ID
     * @return  CustomResponse对象
     */
    @PostMapping("/msg/chat-detailed/delete")
    public CustomResponse delDetail(@RequestParam("id") Integer id) {
        Integer loginUid = currentUser.getUserId();
        CustomResponse customResponse = new CustomResponse();
        if (!chatDetailedService.deleteDetail(id, loginUid)) {
            customResponse.setCode(500);
            customResponse.setMessage("删除消息失败");
        }
        return customResponse;
    }
}
