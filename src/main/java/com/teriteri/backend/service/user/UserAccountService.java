package com.teriteri.backend.service.user;

import com.teriteri.backend.pojo.CustomResponse;

import java.util.Map;

public interface UserAccountService {
    /**
     * 用户注册
     * @param username 账号
     * @param password 密码
     * @param confirmedPassword 确认密码
     * @return CustomResponse对象
     */
    CustomResponse register(String username, String password, String confirmedPassword);

    /**
     * 用户登录
     * @param username 账号
     * @param password 密码
     * @return CustomResponse对象
     */
    CustomResponse login(String username, String password);

    /**
     * 管理员登录
     * @param username 账号
     * @param password 密码
     * @return CustomResponse对象
     */
    CustomResponse adminLogin(String username, String password);

    /**
     * 获取用户个人信息
     * @return CustomResponse对象
     */
    CustomResponse personalInfo();

    /**
     * 获取管理员个人信息
     * @return CustomResponse对象
     */
    CustomResponse adminPersonalInfo();

    /**
     * 退出登录，清空redis中相关用户登录认证
     */
    void logout();

    /**
     * 管理员退出登录，清空redis中相关管理员登录认证
     */
    void adminLogout();
}
