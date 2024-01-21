package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.service.utils.CurrentUser;
import com.teriteri.backend.service.video.UserVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserVideoController {
    @Autowired
    private UserVideoService userVideoService;

    @Autowired
    private CurrentUser currentUser;

    /**
     * 登录用户播放视频时更新播放次数，有30秒更新间隔（防止用户刷播放量）
     * @param vid   视频ID
     * @return  返回用户与该视频的交互数据
     */
    @PostMapping("/video/play/user")
    public CustomResponse newPlayWithLoginUser(@RequestParam("vid") Integer vid) {
        Integer uid = currentUser.getUserId();
        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(userVideoService.updatePlay(uid, vid));
        return customResponse;
    }

    /**
     * 点赞或点踩
     * @param vid   视频ID
     * @param isLove    赞还是踩 true赞 false踩
     * @param isSet     点还是取消 true点 false取消
     * @return 返回用户与该视频更新后的交互数据
     */
    @PostMapping("/video/love-or-not")
    public CustomResponse loveOrNot(@RequestParam("vid") Integer vid,
                                    @RequestParam("isLove") boolean isLove,
                                    @RequestParam("isSet") boolean isSet) {
        Integer uid = currentUser.getUserId();
        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(userVideoService.setLoveOrUnlove(uid, vid, isLove, isSet));
        return customResponse;
    }

}
