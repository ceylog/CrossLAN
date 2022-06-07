package com.wg.crosslan.server.crosslan.server;


import com.wg.crosslan.common.protocol.proto.CrossLanMessage;
import com.wg.crosslan.server.crosslan.handler.CrossLANHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConfigurationProperties(prefix = "crosslan.server")
public class CrossLANServer implements ApplicationRunner, ApplicationListener<ContextClosedEvent>, ApplicationContextAware {
    private Integer port;
    private String bindHost;
    private Channel channel;

    private String token;

    private ApplicationContext applicationContext;

    public void run(ApplicationArguments args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(bindHost, port)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_BACKLOG, 32)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) {
                            channel.pipeline().addLast(
                                    new ProtobufVarint32FrameDecoder(),
                                    new ProtobufDecoder(CrossLanMessage.getDefaultInstance()),
                                    new ProtobufVarint32LengthFieldPrepender(),
                                    new ProtobufEncoder(),
                                    new IdleStateHandler(60, 30, 0),
                                    new CrossLANHandler(token)
                            );
                        }
                    });
            channel = b.bind().sync().channel();
            channel.closeFuture().addListener((ChannelFutureListener) future -> {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            });
            log.info("CrossLAN server started on port:{}", port);
        } catch (InterruptedException e) {
            log.error("Cross LAN server start error",e);
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            throw e;
        }
    }


    public void onApplicationEvent(ContextClosedEvent event) {
        close();
    }

    public synchronized void close() {
        if (null != this.channel) {
            this.channel.close();
        }
        log.info("Cross LAN server stop");
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getBindHost() {
        return bindHost;
    }

    public void setBindHost(String bindHost) {
        this.bindHost = bindHost;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
