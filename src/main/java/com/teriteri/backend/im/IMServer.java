package com.teriteri.backend.im;

import com.teriteri.backend.im.handler.TokenValidationHandler;
import com.teriteri.backend.im.handler.WebSocketHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IMServer {

    // 存储每个用户的全部连接
    public static final Map<Integer, Set<Channel>> userChannel = new ConcurrentHashMap<>();

    public void start() throws InterruptedException {

        // 主从结构
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();

        // 绑定监听端口
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)

                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline(); // 处理流

                        // 添加 Http 编码解码器
                        pipeline.addLast(new HttpServerCodec())
                                // 添加处理大数据的组件
                                .addLast(new ChunkedWriteHandler())
                                // 对 Http 消息做聚合操作方便处理，产生 FullHttpRequest 和 FullHttpResponse
                                // 1024 * 64 是单条信息最长字节数
                                .addLast(new HttpObjectAggregator(1024 * 64))
                                .addLast(new TokenValidationHandler())
                                // 添加 WebSocket 支持
                                .addLast(new WebSocketServerProtocolHandler("/im"))
                                .addLast(new WebSocketHandler());

                    }
                });
        ChannelFuture future = bootstrap.bind(7071).sync();

    }
}
