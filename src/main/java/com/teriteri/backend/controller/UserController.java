package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.service.user.UserService;
import com.teriteri.backend.service.utils.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private CurrentUser currentUser;

    /**
     * 更新用户部分个人信息
     * @param nickname  昵称
     * @param desc  个性签名
     * @param gender    性别：0 女 1 男 2 保密
     * @return
     */
    @PostMapping("/user/info/update")
    public CustomResponse updateUserInfo(@RequestParam("nickname") String nickname,
                                         @RequestParam("description") String desc,
                                         @RequestParam("gender") Integer gender) {
        Integer uid = currentUser.getUserId();
        try {
            return userService.updateUserInfo(uid, nickname, desc, gender);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("特丽丽被玩坏了");
            return customResponse;
        }
    }

    /**
     * 更新用户头像
     * @param file  头像文件
     * @return  成功则返回新头像url
     */
    @PostMapping("/user/avatar/update")
    public CustomResponse updateUserAvatar(@RequestParam("file") MultipartFile file) {
        Integer uid = currentUser.getUserId();
        try {
            return userService.updateUserAvatar(uid, file);
        } catch (Exception e) {
            e.printStackTrace();
            return new CustomResponse(500, "头像更新失败", null);
        }
    }

    @GetMapping("/user/info/get-one")
    public CustomResponse getOneUserInfo(@RequestParam("uid") Integer uid) {
        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(userService.getUserById(uid));
        return customResponse;
    }
}
