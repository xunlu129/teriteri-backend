package com.teriteri.backend.service.utils;

import com.teriteri.backend.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class EventListenerService {

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 轮询用户登录状态，将登录过期但还在在线集合的用户移出集合
     */
    @Scheduled(fixedDelay = 1000 * 60 * 10) // 10分钟轮询一次，记得启动类加上 @EnableScheduling 注解以启动任务调度功能
    public void updateLoginMember() {
        Set<Object> lm = redisUtil.getMembers("login_member");
//        System.out.println(lm);
        for (Object id: lm) {
            if (!redisUtil.isExist("security:user:" + id)) {
                redisUtil.delMember("login_member", id);
            }
        }
    }
}
