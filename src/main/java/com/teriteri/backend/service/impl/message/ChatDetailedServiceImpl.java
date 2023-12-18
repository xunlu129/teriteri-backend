package com.teriteri.backend.service.impl.message;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.teriteri.backend.mapper.ChatDetailedMapper;
import com.teriteri.backend.pojo.ChatDetailed;
import com.teriteri.backend.service.message.ChatDetailedService;
import com.teriteri.backend.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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
     * @param isDesc    是否降序
     * @return  消息列表以及是否还有更多 { list: List, more: boolean }
     */
    @Override
    public Map<String, Object> getDetails(Integer uid, Integer aid, Long offset) {
        String key = "chat_detailed_zset:" + uid + ":" + aid;
        Map<String, Object> map = new HashMap<>();
        if (offset + 20 < redisUtil.zCount(key, 0, Long.MAX_VALUE)) {
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
}
