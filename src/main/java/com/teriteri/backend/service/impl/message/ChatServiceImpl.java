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
     * @param from  发消息者UID (我打开对方的聊天框即对方是发消息者)
     * @param to    收消息者UID (我打开对方的聊天框即我是收消息者)
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
     * @param uid   对方用户ID
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
     * 移除聊天 并清除未读
     * @param from  发消息者UID（对方）
     * @param to    收消息者UID（自己）
     */
    @Override
    public void delChat(Integer from, Integer to) {
        QueryWrapper<Chat> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", from).eq("another_id", to);
        Chat chat = chatMapper.selectOne(queryWrapper);
        if (chat == null) return;
        // 更新字段伪删除
        UpdateWrapper<Chat> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", from).eq("another_id", to).setSql("is_deleted = 1").setSql("unread = 0");
        chatMapper.update(new Chat(), updateWrapper);
        try {
            redisUtil.zsetDelMember("chat_zset:" + to, chat.getId());
        } catch (Exception e) {
            log.error("redis移除聊天失败");
        }
    }

    /**
     * 发送消息时更新对应聊天的未读数和时间
     * @param from  发送者ID（自己）
     * @param to    接受者ID（对方）
     */
    @Override
    public void updateChat(Integer from, Integer to) {
        try {
            /*
             既然我要发消息给对方 那么 to -> from 的 chat 表数据一定是存在的 因为发消息前一定创建了聊天
             所以只要判断 from -> to 的数据是否存在
             */

            // 创建两个线程分别处理对应数据
            // 先更新 to -> from 的数据
            CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
                QueryWrapper<Chat> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.eq("user_id", to).eq("another_id", from);
                Chat chat1 = chatMapper.selectOne(queryWrapper1);
                UpdateWrapper<Chat> updateWrapper1 = new UpdateWrapper<>();
                updateWrapper1.eq("user_id", to)
                        .eq("another_id", from)
                        .set("latest_time", new Date());
                chatMapper.update(null, updateWrapper1);
                redisUtil.zset("chat_zset:" + from, chat1.getId());    // 添加到这个用户的最近聊天的有序集合
            }, taskExecutor);

            // 再查询 from -> to 的数据
            CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
                QueryWrapper<Chat> queryWrapper2 = new QueryWrapper<>();
                queryWrapper2.eq("user_id", from).eq("another_id", to);
                Chat chat2 = chatMapper.selectOne(queryWrapper2);

                // 查询对方是否在窗口
                String key = "whisper:" + to + ":" + from;  // whisper:用户自己:聊天对象 这里的用户自己就是对方本人 聊天对象就是在发消息的我自己
                if (redisUtil.isExist(key)) {
                    // 如果对方在窗口就不更新未读
                    if (chat2 == null) {
                        // 如果对方没聊过天 就创建聊天
                        chat2 = new Chat(null, from, to, 0, 0, new Date());
                        chatMapper.insert(chat2);
                    } else {
                        // 如果聊过 就只更新时间
                        UpdateWrapper<Chat> updateWrapper2 = new UpdateWrapper<>();
                        updateWrapper2.eq("id", chat2.getId())
                                .set("latest_time", new Date());
                        chatMapper.update(null, updateWrapper2);
                    }
                    redisUtil.zset("chat_zset:" + to, chat2.getId());    // 添加到这个用户的最近聊天的有序集合
                } else {
                    // 如果不在窗口就未读+1
                    if (chat2 == null) {
                        // 如果对方没聊过天 就创建聊天
                        chat2 = new Chat(null, from, to, 0, 1, new Date());
                        chatMapper.insert(chat2);
                    } else {
                        // 如果聊过 就更新未读和时间
                        UpdateWrapper<Chat> updateWrapper2 = new UpdateWrapper<>();
                        updateWrapper2.eq("id", chat2.getId())
                                .setSql("unread = unread + 1")
                                .set("latest_time", new Date());
                        chatMapper.update(null, updateWrapper2);
                    }
                    redisUtil.zset("chat_zset:" + to, chat2.getId());    // 添加到这个用户的最近聊天的有序集合
                }
            }, taskExecutor);

            future1.join();
            future2.join();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新窗口为在线状态，顺便清除未读
     * @param from  发消息者UID（对方）
     * @param to    收消息者UID（自己）
     */
    @Override
    public void updateWhisperOnline(Integer from, Integer to) {
        try {
            String key = "whisper:" + to + ":" + from;  // whisper:用户自己:聊天对象 这里的用户自己就是对方本人 聊天对象就是在发消息的我自己
            // 更新为在线状态
            redisUtil.setValue(key, true);
            // 清除未读
            UpdateWrapper<Chat> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("user_id", from).eq("another_id", to).set("unread", 0);
            chatMapper.update(null, updateWrapper);
        } catch (Exception e) {
            log.error("更新聊天窗口在线状态失败: " + e);
        }
    }

    /**
     * 更新窗口为离开状态
     * @param from  发消息者UID（对方）
     * @param to    收消息者UID（自己）
     */
    @Override
    public void updateWhisperOutline(Integer from, Integer to) {
        try {
            String key = "whisper:" + to + ":" + from;  // whisper:用户自己:聊天对象 这里的用户自己就是对方本人 聊天对象就是在发消息的我自己
            // 删除key更新为离开状态
            redisUtil.delValue(key);
            System.out.println("用户 " + to + " 离开 " + from + " 的窗口");
        } catch (Exception e) {
            log.error("更新聊天窗口在线状态失败: " + e);
        }
    }
}
