package com.teriteri.backend.im.handler;

import com.alibaba.fastjson2.JSON;
import com.teriteri.backend.im.IMServer;
import com.teriteri.backend.pojo.Command;
import com.teriteri.backend.pojo.IMResponse;
import com.teriteri.backend.pojo.User;
import com.teriteri.backend.utils.JwtUtil;
import com.teriteri.backend.utils.RedisUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class TokenValidationHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static JwtUtil jwtUtil;
    private static RedisUtil redisUtil;

    @Autowired
    public void setDependencies(JwtUtil jwtUtil, RedisUtil redisUtil) {
        TokenValidationHandler.jwtUtil = jwtUtil;
        TokenValidationHandler.redisUtil = redisUtil;
    }

    public TokenValidationHandler() {

    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame tx) {
        Command command = JSON.parseObject(tx.text(), Command.class);
        String token = command.getContent();

        Integer uid = isValidToken(token);
        if (uid != null) {
            // 将uid绑到ctx上
            ctx.channel().attr(AttributeKey.valueOf("userId")).set(uid);
            // 将channel存起来
            if (IMServer.userChannel.get(uid) == null) {
                Set<Channel> set = new HashSet<>();
                set.add(ctx.channel());
                IMServer.userChannel.put(uid, set);
            } else {
                IMServer.userChannel.get(uid).add(ctx.channel());
            }
            redisUtil.addMember("login_member", uid);   // 将用户添加到在线用户集合
//            System.out.println("该用户的全部连接状态：" + IMServer.userChannel.get(uid));
//            System.out.println("当前在线人数：" + IMServer.userChannel.size());
            // 移除token验证处理器，以便以后使用无需判断
            ctx.pipeline().remove(TokenValidationHandler.class);
            // 保持消息的引用计数，以确保消息不会被释放
            tx.retain();
            // 将消息传递给下一个处理器
            ctx.fireChannelRead(tx);
        } else {
            ctx.channel().writeAndFlush(IMResponse.error("登录已过期"));
            ctx.close();
        }
    }

    /**
     * 进行JWT验证
     * @param token Bearer JWT
     * @return  返回用户ID 验证不通过则返回null
     */
    private Integer isValidToken(String token) {
        if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {
            return null;
        }

        token = token.substring(7);

        // 解析token
        boolean verifyToken = jwtUtil.verifyToken(token);
        if (!verifyToken) {
            log.error("当前token已过期");
            return null;
        }
        String userId = JwtUtil.getSubjectFromToken(token);
        String role = JwtUtil.getClaimFromToken(token, "role");
        User user = redisUtil.getObject("security:" + role + ":" + userId, User.class);

        if (user == null) {
            log.error("用户未登录");
            return null;
        }

//        log.info("成功通过验证！");
        return user.getUid();
    }

}
