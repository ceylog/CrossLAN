package com.wg.crosslan.server.crosslan.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProxyServer {
    private Channel channel;

    public synchronized void bind(int port, ChannelInitializer<SocketChannel> channelInitializer) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(channelInitializer)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            channel = b.bind(port).sync().channel();
        } catch (InterruptedException e) {
            log.error("proxy server start faild", e);
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            throw e;
        }
    }

    public synchronized void close() {
        if (null != channel) {
            channel.close();
        }
    }
}
