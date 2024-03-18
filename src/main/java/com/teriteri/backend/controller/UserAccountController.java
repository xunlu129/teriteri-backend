package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.service.user.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class UserAccountController {

    @Autowired
    private UserAccountService userAccountService;

    /**
     * 注册接口
     * @param map 包含 username password confirmedPassword 的 map
     * @return CustomResponse对象
     */
    // 前端使用axios传递的data是Content-Type: application/json，需要用@RequestBody获取参数
    @PostMapping("/user/account/register")
    public CustomResponse register(@RequestBody Map<String, String> map) {
        String username = map.get("username");
        String password = map.get("password");
        String confirmedPassword = map.get("confirmedPassword");
        try {
            return userAccountService.register(username, password, confirmedPassword);
        } catch (Exception e) {
            e.printStackTrace();
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("特丽丽被玩坏了");
            return customResponse;
        }
    }

    /**
     * 登录接口
     * @param map 包含 username password 的 map
     * @return CustomResponse对象
     */
    @PostMapping("/user/account/login")
    public CustomResponse login(@RequestBody Map<String, String> map) {
        String username = map.get("username");
        String password = map.get("password");
        return userAccountService.login(username, password);
    }

    /**
     * 管理员登录接口
     * @param map 包含 username password 的 map
     * @return CustomResponse对象
     */
    @PostMapping("/admin/account/login")
    public CustomResponse adminLogin(@RequestBody Map<String, String> map) {
        String username = map.get("username");
        String password = map.get("password");
        return userAccountService.adminLogin(username, password);
    }

    /**
     * 获取当前登录用户信息接口
     * @return CustomResponse对象
     */
    @GetMapping("/user/personal/info")
    public CustomResponse personalInfo() {
        return userAccountService.personalInfo();
    }

    /**
     * 获取当前登录管理员信息接口
     * @return CustomResponse对象
     */
    @GetMapping("/admin/personal/info")
    public CustomResponse adminPersonalInfo() {
        return userAccountService.adminPersonalInfo();
    }

    /**
     * 退出登录接口
     */
    @GetMapping("/user/account/logout")
    public void logout() {
        userAccountService.logout();
    }

    /**
     * 管理员退出登录接口
     */
    @GetMapping("/admin/account/logout")
    public void adminLogout() {
        userAccountService.adminLogout();
    }

    /**
     * 修改当前用户密码
     * @param pw    就密码
     * @param npw   新密码
     * @return  响应对象
     */
    @PostMapping("/user/password/update")
    public CustomResponse updatePassword(@RequestParam("pw") String pw, @RequestParam("npw") String npw) {
        return userAccountService.updatePassword(pw, npw);
    }
}
