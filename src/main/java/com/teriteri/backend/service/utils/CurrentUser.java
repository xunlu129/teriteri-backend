package com.teriteri.backend.service.utils;

import com.teriteri.backend.mapper.UserMapper;
import com.teriteri.backend.pojo.User;
import com.teriteri.backend.service.impl.user.UserDetailsImpl;
import com.teriteri.backend.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUser {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取当前登录用户的uid，也是JWT认证的一环
     * @return 当前登录用户的uid
     */
    public Integer getUserId() {
        UsernamePasswordAuthenticationToken authenticationToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticationToken.getPrincipal();
        User suser = loginUser.getUser();   // 这里的user是登录时存的security:user，因为是静态数据，可能会跟实际的有区别，所以只能用作获取uid用
        return suser.getUid();
    }

    /**
     * 判断当前用户是否管理员
     * @return  是否管理员 true/false
     */
    public Boolean isAdmin() {
        Integer uid = getUserId();
        User user = redisUtil.getObject("user:" + uid, User.class);
        if (user == null) {
            user = userMapper.selectById(uid);
            redisUtil.setExObjectValue("user:" + user.getUid(), user);
        }
        return (user.getRole() == 1 || user.getRole() == 2);
    }
}
