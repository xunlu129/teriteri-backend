package com.teriteri.backend.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.teriteri.backend.im.IMServer;
import com.teriteri.backend.mapper.FavoriteMapper;
import com.teriteri.backend.mapper.MsgUnreadMapper;
import com.teriteri.backend.mapper.UserMapper;
import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.Favorite;
import com.teriteri.backend.pojo.MsgUnread;
import com.teriteri.backend.pojo.User;
import com.teriteri.backend.pojo.dto.UserDTO;
import com.teriteri.backend.service.user.UserAccountService;
import com.teriteri.backend.service.user.UserService;
import com.teriteri.backend.service.utils.CurrentUser;
import com.teriteri.backend.utils.ESUtil;
import com.teriteri.backend.utils.JwtUtil;
import com.teriteri.backend.utils.RedisUtil;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserAccountServiceImpl implements UserAccountService {
    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MsgUnreadMapper msgUnreadMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ESUtil esUtil;

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    /**
     * 用户注册
     * @param username 账号
     * @param password 密码
     * @param confirmedPassword 确认密码
     * @return CustomResponse对象
     */
    @Override
    @Transactional
    public CustomResponse register(String username, String password, String confirmedPassword) throws IOException {
        CustomResponse customResponse = new CustomResponse();
        if (username == null) {
            customResponse.setCode(403);
            customResponse.setMessage("账号不能为空");
            return customResponse;
        }
        if (password == null || confirmedPassword == null) {
            customResponse.setCode(403);
            customResponse.setMessage("密码不能为空");
            return customResponse;
        }
        username = username.trim();   //删掉用户名的空白符
        if (username.length() == 0) {
            customResponse.setCode(403);
            customResponse.setMessage("账号不能为空");
            return customResponse;
        }
        if (username.length() > 50) {
            customResponse.setCode(403);
            customResponse.setMessage("账号长度不能大于50");
            return customResponse;
        }
        if (password.length() == 0 || confirmedPassword.length() == 0 ) {
            customResponse.setCode(403);
            customResponse.setMessage("密码不能为空");
            return customResponse;
        }
        if (password.length() > 50 || confirmedPassword.length() > 50 ) {
            customResponse.setCode(403);
            customResponse.setMessage("密码长度不能大于50");
            return customResponse;
        }
        if (!password.equals(confirmedPassword)) {
            customResponse.setCode(403);
            customResponse.setMessage("两次输入的密码不一致");
            return customResponse;
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.ne("state", 2);
        User user = userMapper.selectOne(queryWrapper);   //查询数据库里值等于username并且没有注销的数据
        if (user != null) {
            customResponse.setCode(403);
            customResponse.setMessage("账号已存在");
            return customResponse;
        }

        QueryWrapper<User> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.orderByDesc("uid").last("limit 1");    // 降序选第一个
        User last_user = userMapper.selectOne(queryWrapper1);
        int new_user_uid;
        if (last_user == null) {
            new_user_uid = 1;
        } else {
            new_user_uid = last_user.getUid() + 1;
        }
        String encodedPassword = passwordEncoder.encode(password);  // 密文存储
        String avatar_url = "https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png";
        String bg_url = "https://tinypic.host/images/2023/11/15/69PB2Q5W9D2U7L.png";
        Date now = new Date();
        User new_user = new User(
                null,
                username,
                encodedPassword,
                "用户_" + new_user_uid,
                avatar_url,
                bg_url,
                2,
                "这个人很懒，什么都没留下~",
                0,
                (double) 0,
                0,
                0,
                0,
                0,
                null,
                now,
                null
        );
        userMapper.insert(new_user);
        msgUnreadMapper.insert(new MsgUnread(new_user.getUid(),0,0,0,0,0,0));
        favoriteMapper.insert(new Favorite(null, new_user.getUid(), 1, 1, null, "默认收藏夹", "", 0, null));
        esUtil.addUser(new_user);
        customResponse.setMessage("注册成功！欢迎加入T站");
        return customResponse;
    }

    /**
     * 用户登录
     * @param username 账号
     * @param password 密码
     * @return CustomResponse对象
     */
    @Override
    public CustomResponse login(String username, String password) {
        CustomResponse customResponse = new CustomResponse();

        //验证是否能正常登录
        //将用户名和密码封装成一个类，这个类不会存明文了，将是加密后的字符串
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);

        // 用户名或密码错误会抛出异常
        Authentication authenticate;
        try {
            authenticate = authenticationProvider.authenticate(authenticationToken);
        } catch (Exception e) {
            customResponse.setCode(403);
            customResponse.setMessage("账号或密码不正确");
            return customResponse;
        }

        //将用户取出来
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticate.getPrincipal();
        User user = loginUser.getUser();

        // 顺便更新redis中的数据
        redisUtil.setExObjectValue("user:" + user.getUid(), user);  // 默认存活1小时

        // 检查账号状态，1 表示封禁中，不允许登录
        if (user.getState() == 1) {
            customResponse.setCode(403);
            customResponse.setMessage("账号异常，封禁中");
            return customResponse;
        }

        //将uid封装成一个jwttoken，同时token也会被缓存到redis中
        String token = jwtUtil.createToken(user.getUid().toString(), "user");

        try {
            // 把完整的用户信息存入redis，时间跟token一样，注意单位
            // 这里缓存的user信息建议只供读取uid用，其中的状态等非静态数据可能不准，所以 redis另外存值
            redisUtil.setExObjectValue("security:user:" + user.getUid(), user, 60L * 60 * 24 * 2, TimeUnit.SECONDS);
            // 将该用户放到redis中在线集合
//            redisUtil.addMember("login_member", user.getUid());
        } catch (Exception e) {
            log.error("存储redis数据失败");
            throw e;
        }

        // 每次登录顺便返回user信息，就省去再次发送一次获取用户个人信息的请求
        UserDTO userDTO = new UserDTO();
        userDTO.setUid(user.getUid());
        userDTO.setNickname(user.getNickname());
        userDTO.setAvatar_url(user.getAvatar());
        userDTO.setBg_url(user.getBackground());
        userDTO.setGender(user.getGender());
        userDTO.setDescription(user.getDescription());
        userDTO.setExp(user.getExp());
        userDTO.setCoin(user.getCoin());
        userDTO.setVip(user.getVip());
        userDTO.setState(user.getState());
        userDTO.setAuth(user.getAuth());
        userDTO.setAuthMsg(user.getAuthMsg());

        Map<String, Object> final_map = new HashMap<>();
        final_map.put("token", token);
        final_map.put("user", userDTO);
        customResponse.setMessage("登录成功");
        customResponse.setData(final_map);
        return customResponse;
    }

    /**
     * 管理员登录
     * @param username 账号
     * @param password 密码
     * @return CustomResponse对象
     */
    @Override
    public CustomResponse adminLogin(String username, String password) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);
        Authentication authenticate = authenticationProvider.authenticate(authenticationToken);
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticate.getPrincipal();
        User user = loginUser.getUser();
        CustomResponse customResponse = new CustomResponse();
        // 普通用户无权访问
        if (user.getRole() == 0) {
            customResponse.setCode(403);
            customResponse.setMessage("您不是管理员，无权访问");
            return customResponse;
        }
        // 顺便更新redis中的数据
        redisUtil.setExObjectValue("user:" + user.getUid(), user);  // 默认存活1小时
        // 检查账号状态，1 表示封禁中，不允许登录
        if (user.getState() == 1) {
            customResponse.setCode(403);
            customResponse.setMessage("账号异常，封禁中");
            return customResponse;
        }
        //将uid封装成一个jwttoken，同时token也会被缓存到redis中
        String token = jwtUtil.createToken(user.getUid().toString(), "admin");
        try {
            redisUtil.setExObjectValue("security:admin:" + user.getUid(), user, 60L * 60 * 24 * 2, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("存储redis数据失败");
            throw e;
        }
        // 每次登录顺便返回user信息，就省去再次发送一次获取用户个人信息的请求
        UserDTO userDTO = new UserDTO();
        userDTO.setUid(user.getUid());
        userDTO.setNickname(user.getNickname());
        userDTO.setAvatar_url(user.getAvatar());
        userDTO.setBg_url(user.getBackground());
        userDTO.setGender(user.getGender());
        userDTO.setDescription(user.getDescription());
        userDTO.setExp(user.getExp());
        userDTO.setCoin(user.getCoin());
        userDTO.setVip(user.getVip());
        userDTO.setState(user.getState());
        userDTO.setAuth(user.getAuth());
        userDTO.setAuthMsg(user.getAuthMsg());

        Map<String, Object> final_map = new HashMap<>();
        final_map.put("token", token);
        final_map.put("user", userDTO);
        customResponse.setMessage("欢迎回来，主人≥⏝⏝≤");
        customResponse.setData(final_map);
        return customResponse;
    }

    /**
     * 获取用户个人信息
     * @return CustomResponse对象
     */
    @Override
    public CustomResponse personalInfo() {
        Integer loginUserId = currentUser.getUserId();
        UserDTO userDTO = userService.getUserById(loginUserId);

        CustomResponse customResponse = new CustomResponse();
        // 检查账号状态，1 表示封禁中，不允许登录，2表示账号注销了
        if (userDTO.getState() == 2) {
            customResponse.setCode(404);
            customResponse.setMessage("账号已注销");
            return customResponse;
        }
        if (userDTO.getState() == 1) {
            customResponse.setCode(403);
            customResponse.setMessage("账号异常，封禁中");
            return customResponse;
        }

        customResponse.setData(userDTO);
        return customResponse;
    }

    /**
     * 获取管理员个人信息
     * @return CustomResponse对象
     */
    @Override
    public CustomResponse adminPersonalInfo() {
        Integer LoginUserId = currentUser.getUserId();
        // 从redis中获取最新数据
        User user = redisUtil.getObject("user:" + LoginUserId, User.class);
        // 如果redis中没有user数据，就从mysql中获取并更新到redis
        if (user == null) {
            user = userMapper.selectById(LoginUserId);
            User finalUser = user;
            CompletableFuture.runAsync(() -> {
                redisUtil.setExObjectValue("user:" + finalUser.getUid(), finalUser);  // 默认存活1小时
            }, taskExecutor);
        }
        CustomResponse customResponse = new CustomResponse();

        // 普通用户无权访问
        if (user.getRole() == 0) {
            customResponse.setCode(403);
            customResponse.setMessage("您不是管理员，无权访问");
            return customResponse;
        }
        // 检查账号状态，1 表示封禁中，不允许登录，2表示已注销
        if (user.getState() == 2) {
            customResponse.setCode(404);
            customResponse.setMessage("账号已注销");
            return customResponse;
        }
        if (user.getState() == 1) {
            customResponse.setCode(403);
            customResponse.setMessage("账号异常，封禁中");
            return customResponse;
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setUid(user.getUid());
        userDTO.setNickname(user.getNickname());
        userDTO.setAvatar_url(user.getAvatar());
        userDTO.setBg_url(user.getBackground());
        userDTO.setGender(user.getGender());
        userDTO.setDescription(user.getDescription());
        userDTO.setExp(user.getExp());
        userDTO.setCoin(user.getCoin());
        userDTO.setVip(user.getVip());
        userDTO.setState(user.getState());
        userDTO.setAuth(user.getAuth());
        userDTO.setAuthMsg(user.getAuthMsg());
        customResponse.setData(userDTO);
        return customResponse;
    }

    /**
     * 退出登录，清空redis中相关用户登录认证
     */
    @Override
    public void logout() {
        Integer LoginUserId = currentUser.getUserId();
        // 清除redis中该用户的登录认证数据
        redisUtil.delValue("token:user:" + LoginUserId);
        redisUtil.delValue("security:user:" + LoginUserId);
        redisUtil.delMember("login_member", LoginUserId);   // 从在线用户集合中移除
        redisUtil.deleteKeysWithPrefix("whisper:" + LoginUserId + ":"); // 清除全部在聊天窗口的状态

        // 断开全部该用户的channel 并从 userChannel 移除该用户
        Set<Channel> userChannels = IMServer.userChannel.get(LoginUserId);
        if (userChannels != null) {
            for (Channel channel : userChannels) {
                try {
                    channel.close().sync(); // 等待通道关闭完成
                } catch (InterruptedException e) {
                    // 处理异常，如果有必要的话
                    e.printStackTrace();
                }
            }
            IMServer.userChannel.remove(LoginUserId);
        }
    }

    /**
     * 管理员退出登录，清空redis中相关管理员登录认证
     */
    @Override
    public void adminLogout() {
        Integer LoginUserId = currentUser.getUserId();
        // 清除redis中该用户的登录认证数据
        redisUtil.delValue("token:admin:" + LoginUserId);
        redisUtil.delValue("security:admin:" + LoginUserId);
    }

    @Override
    public CustomResponse updatePassword(String pw, String npw) {
        CustomResponse customResponse = new CustomResponse();
        if (npw == null || npw.length() == 0) {
            customResponse.setCode(500);
            customResponse.setMessage("密码不能为空");
            return customResponse;
        }

        // 取出当前登录的用户
        UsernamePasswordAuthenticationToken authenticationToken1 =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails1 = (UserDetailsImpl) authenticationToken1.getPrincipal();
        User user = userDetails1.getUser();

        // 验证旧密码
        UsernamePasswordAuthenticationToken authenticationToken2 =
                new UsernamePasswordAuthenticationToken(user.getUsername(), pw);
        try {
            authenticationProvider.authenticate(authenticationToken2);
        } catch (Exception e) {
            customResponse.setCode(403);
            customResponse.setMessage("密码不正确");
            return customResponse;
        }

        if (Objects.equals(pw, npw)) {
            customResponse.setCode(500);
            customResponse.setMessage("新密码不能与旧密码相同");
            return customResponse;
        }

        String encodedPassword = passwordEncoder.encode(npw);  // 密文存储

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", user.getUid()).set("password", encodedPassword);
        userMapper.update(null, updateWrapper);

        logout();
        adminLogout();
        return customResponse;
    }
}
