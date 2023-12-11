package com.teriteri.backend.service.impl.message;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.teriteri.backend.mapper.ChatMapper;
import com.teriteri.backend.mapper.UserMapper;
import com.teriteri.backend.pojo.Chat;
import com.teriteri.backend.pojo.User;
import com.teriteri.backend.service.message.ChatService;
import com.teriteri.backend.service.user.UserService;
import com.teriteri.backend.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {
    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    /**
     * 创建聊天
     * @param from  发消息者UID
     * @param to    收消息者UID
     * @return "已存在"/"新创建"
     */
    @Override
    public Map<String, Object> createChat(Integer from, Integer to) {
        Map<String, Object> map = new HashMap<>();
        QueryWrapper<Chat> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", from).eq("another_id", to);
        Chat chat = chatMapper.selectOne(queryWrapper);
        if (chat != null) {
            // 曾经创建过
            if (chat.getIsDeleted() == 1) {
                // 但是被移除状态 重新开启
                chat.setIsDeleted(0);
                chat.setLatestTime(new Date());
                chatMapper.updateById(chat);
                redisUtil.zset("chat_zset:" + to, chat.getId());    // 添加到这个用户的最近聊天的有序集合 最好启动定时任务确保数据一致性
                // 携带信息返回
                map.put("chat", chat);
                Chat finalChat = chat;
                CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
                    map.put("user", userService.getUserById(finalChat.getUserId()));
                }, taskExecutor);
                map.put("last", "不想前进的时候就暂且停下脚步吧");
                map.put("msg", "新创建");
                userFuture.join();
                return map;
            } else {
                // 处于最近聊天中
                map.put("msg", "已存在");
                return map;
            }
        } else {
            // 不存在记录
            // 查询对方用户是否存在
            QueryWrapper<User> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.orderByDesc("uid").last("LIMIT 1");
            User user = userMapper.selectOne(queryWrapper1);
            if (from > user.getUid()) {
                map.put("msg", "未知用户");
                return map;
            }
            // 需新创建
            chat = new Chat(null, from, to, 0, 0, new Date());
            chatMapper.insert(chat);
            redisUtil.zset("chat_zset:" + to, chat.getId());    // 添加到这个用户的最近聊天的有序集合
            // 携带信息返回
            map.put("chat", chat);
            Chat finalChat = chat;
            CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
                map.put("user", userService.getUserById(finalChat.getUserId()));
            }, taskExecutor);
            map.put("last", "不想前进的时候就暂且停下脚步吧");
            map.put("msg", "新创建");
            userFuture.join();
            return map;
        }
    }

    /**
     * 获取聊天列表 包含用户信息和最近一条聊天内容 每次查10个
     * @param uid   用户ID
     * @param offset    查询偏移量（最近聊天的第几个开始往后查）
     * @return  包含用户信息和最近一条聊天内容的聊天列表
     */
    @Override
    public List<Map<String, Object>> getChatListWithData(Integer uid, Long offset) {
        Set<Object> set = redisUtil.zReverange("chat_zset:" + uid, offset, offset + 9);
        // 没有数据则返回空列表
        if (set == null || set.isEmpty()) return Collections.emptyList();
        // 查询
        QueryWrapper<Chat> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", set).eq("is_deleted", 0).orderByDesc("latest_time");
        List<Chat> chatList = chatMapper.selectList(queryWrapper);
        // 没有数据则返回空列表
        if (chatList == null || chatList.isEmpty()) return Collections.emptyList();

        // 封装返回
        Stream<Chat> chatStream = chatList.stream();
        return chatStream.parallel()
                .map(chat -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("chat", chat);

                    CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
                        map.put("user", userService.getUserById(chat.getUserId()));
                    }, taskExecutor);

                    map.put("last", "不想前进的时候就暂且停下脚步吧");

                    userFuture.join();
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * 移除聊天
     * @param from  发消息者UID
     * @param to    收消息者UID
     */
    @Override
    public void delChat(Integer from, Integer to) {
        QueryWrapper<Chat> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", from).eq("another_id", to);
        Chat chat = chatMapper.selectOne(queryWrapper);
        if (chat == null) return;
        // 更新字段伪删除
        UpdateWrapper<Chat> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", from).eq("another_id", to).setSql("is_deleted = 1");
        chatMapper.update(new Chat(), updateWrapper);
        try {
            redisUtil.zsetDelMember("chat_zset:" + to, chat.getId());
        } catch (Exception e) {
            log.error("redis移除聊天失败");
        }
    }
}
