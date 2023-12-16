package com.teriteri.backend.im.handler;

import com.alibaba.fastjson2.JSON;
import com.teriteri.backend.im.IMServer;
import com.teriteri.backend.pojo.Command;
import com.teriteri.backend.pojo.CommandType;
import com.teriteri.backend.pojo.IMResponse;
import com.teriteri.backend.utils.RedisUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static RedisUtil redisUtil;

    @Autowired
    public void setDependencies(RedisUtil redisUtil) {
        WebSocketHandler.redisUtil = redisUtil;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame tx) {

        try {
            Command command = JSON.parseObject(tx.text(), Command.class);
//            System.out.println("command: " + command);
            // 根据code分发不同处理程序
            switch (CommandType.match(command.getCode())) {
                case CONNETION: // 如果是连接消息就不需要做任何操作了，因为连接上的话在token鉴权那就做了
                    break;
                case CHAT_SEND:
                    ChatHandler.send(ctx, tx);
                    break;
                case CHAT_WITHDRAW:
                    ChatHandler.withdraw(ctx, tx);
                    break;
                default: ctx.channel().writeAndFlush(IMResponse.error("不支持的CODE " + command.getCode()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 连接断开时执行 将channel从集合中移除 如果集合为空则从Map中移除该用户 即离线状态
     * @param ctx
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        // 当连接断开时，从 userChannel 中移除对应的 Channel
        Integer uid = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        Set<Channel> userChannels = IMServer.userChannel.get(uid);
//        System.out.println("移除channel前的集合状态：" + userChannels);
        if (userChannels != null) {
            userChannels.remove(ctx.channel());
//            System.out.println("移除channel后的集合状态：" + IMServer.userChannel.get(uid));
            // 用户离线操作
            if (IMServer.userChannel.get(uid).size() == 0) {
                IMServer.userChannel.remove(uid);
//                System.out.println("当前在线人数：" + IMServer.userChannel.size());
                redisUtil.deleteKeysWithPrefix("whisper:" + uid + ":"); // 清除全部在聊天窗口的状态
                redisUtil.delMember("login_member", uid);   // 从在线用户集合中移除
            }
        }
        // 继续处理后续逻辑
        ctx.fireChannelInactive();
    }
}
