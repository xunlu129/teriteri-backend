package com.teriteri.backend.service.impl.message;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.teriteri.backend.mapper.ChatDetailedMapper;
import com.teriteri.backend.pojo.ChatDetailed;
import com.teriteri.backend.service.message.ChatDetailedService;
import com.teriteri.backend.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class ChatDetailedServiceImpl implements ChatDetailedService {
    @Autowired
    private ChatDetailedMapper chatDetailedMapper;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 获取当前聊天的20条消息
     * @param uid   发消息者UID（对方）
     * @param aid   收消息者UID（自己）
     * @param offset    偏移量 从哪条开始数（已经查过了几条）
     * @return  消息列表以及是否还有更多 { list: List, more: boolean }
     */
    @Override
    public Map<String, Object> getDetails(Integer uid, Integer aid, Long offset) {
        String key = "chat_detailed_zset:" + uid + ":" + aid;
        Map<String, Object> map = new HashMap<>();
        if (offset + 20 < redisUtil.zCard(key)) {
            map.put("more", true);
        } else {
            map.put("more", false);
        }
        Set<Object> set = redisUtil.zReverange(key, offset, offset + 19);
        // 没有数据则返回空列表
        if (set == null || set.isEmpty()) {
            map.put("list", Collections.emptyList());
            return map;
        }
        QueryWrapper<ChatDetailed> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", set);
        map.put("list", chatDetailedMapper.selectList(queryWrapper));
        return map;
    }

    /**
     * 删除单条消息记录
     * @param id 消息记录的id
     * @param uid   当前登录用户的UID（自己）
     * @return  成功/失败
     */
    @Override
    public boolean deleteDetail(Integer id, Integer uid) {
        try {
            // 查询 查不到数据或者发送者和接收者都不是登录用户就删除失败
            ChatDetailed chatDetailed = chatDetailedMapper.selectById(id);
            if (chatDetailed == null) return false;
            UpdateWrapper<ChatDetailed> updateWrapper = new UpdateWrapper<>();
            if (Objects.equals(chatDetailed.getUserId(), uid)) {
                // 如果删除的消息是自己发送的
                updateWrapper.eq("id", id).setSql("user_del = 1");
                chatDetailedMapper.update(null, updateWrapper);
                String key = "chat_detailed_zset:" + chatDetailed.getAnotherId() + ":" + uid;
                redisUtil.zsetDelMember(key, id);
                return true;
            } else if (Objects.equals(chatDetailed.getAnotherId(), uid)) {
                // 如果删除的消息是对方发送的
                updateWrapper.eq("id", id).setSql("another_del = 1");
                chatDetailedMapper.update(null, updateWrapper);
                String key = "chat_detailed_zset:" + chatDetailed.getUserId() + ":" + uid;
                redisUtil.zsetDelMember(key, id);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("删除消息记录时出错了" + e);
            return false;
        }
    }
}
