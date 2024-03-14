package com.teriteri.backend.service.impl.message;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.teriteri.backend.im.IMServer;
import com.teriteri.backend.mapper.ChatMapper;
import com.teriteri.backend.mapper.MsgUnreadMapper;
import com.teriteri.backend.pojo.Chat;
import com.teriteri.backend.pojo.IMResponse;
import com.teriteri.backend.pojo.MsgUnread;
import com.teriteri.backend.service.message.MsgUnreadService;
import com.teriteri.backend.utils.RedisUtil;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class MsgUnreadServiceImpl implements MsgUnreadService {

    @Autowired
    private MsgUnreadMapper msgUnreadMapper;

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    /**
     * 给指定用户的某一列未读消息加一
     * @param uid   用户ID
     * @param column    msg_unread表列名 "reply"/"at"/"love"/"system"/"whisper"/"dynamic"
     */
    @Override
    public void addOneUnread(Integer uid, String column) {
        UpdateWrapper<MsgUnread> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", uid).setSql(column + " = " + column + " + 1");
        msgUnreadMapper.update(null, updateWrapper);
        redisUtil.delValue("msg_unread:" + uid);
    }

    /**
     * 清除指定用户的某一列未读消息
     * @param uid   用户ID
     * @param column    msg_unread表列名 "reply"/"at"/"love"/"system"/"whisper"/"dynamic"
     */
    @Override
    public void clearUnread(Integer uid, String column) {
        QueryWrapper<MsgUnread> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid).ne(column, 0);
        MsgUnread msgUnread = msgUnreadMapper.selectOne(queryWrapper);
        // 如果本身就是0条未读就没必要执行下面的操作了 不过如果有未读的话做这个查询就会带来额外的开销
        if (msgUnread == null) return;

        // 通知用户的全部channel 更新该消息类型未读数为0
        Map<String, Object> map = new HashMap<>();
        map.put("type", "全部已读");
        Set<Channel> myChannels = IMServer.userChannel.get(uid);
        if (myChannels != null) {
            for (Channel channel : myChannels) {
                channel.writeAndFlush(IMResponse.message(column, map));
            }
        }

        UpdateWrapper<MsgUnread> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", uid).set(column, 0);
        msgUnreadMapper.update(null, updateWrapper);
        redisUtil.delValue("msg_unread:" + uid);
        if (Objects.equals(column, "whisper")) {
            // 如果是清除私聊消息还需要去把chat表的全部未读清掉
            UpdateWrapper<Chat> updateWrapper1 = new UpdateWrapper<>();
            updateWrapper1.eq("another_id", uid).set("unread", 0);
            chatMapper.update(null, updateWrapper1);
        }
    }

    /**
     * 私聊消息特有的减除一定数量的未读数
     * @param uid   用户ID
     * @param count 要减多少
     */
    @Override
    public void subtractWhisper(Integer uid, Integer count) {
        UpdateWrapper<MsgUnread> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", uid)
                // 更新后的未读数不能小于0
                .setSql("whisper = CASE WHEN whisper - " + count + " < 0 THEN 0 ELSE whisper - " + count + " END");
        msgUnreadMapper.update(null, updateWrapper);
        redisUtil.delValue("msg_unread:" + uid);
    }

    /**
     * 获取某人的全部消息未读数
     * @param uid   用户ID
     * @return  MsgUnread对象
     */
    @Override
    public MsgUnread getUnread(Integer uid) {
        MsgUnread msgUnread = redisUtil.getObject("msg_unread:" + uid, MsgUnread.class);
        if (msgUnread == null) {
            msgUnread = msgUnreadMapper.selectById(uid);
            if (msgUnread != null) {
                MsgUnread finalMsgUnread = msgUnread;
                CompletableFuture.runAsync(() -> {
                    redisUtil.setExObjectValue("msg_unread:" + uid, finalMsgUnread);    // 异步更新到redis
                }, taskExecutor);
            } else {
                return new MsgUnread(uid,0,0,0,0,0,0);
            }
        }
        return msgUnread;
    }

}
