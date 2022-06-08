package com.wg.crosslan.client.client;

import com.wg.crosslan.client.handler.CrossLanClientHandler;
import com.wg.crosslan.common.protocol.proto.CrossLanMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConfigurationProperties(prefix = "crosslan.client")
public class CrossLanClient implements ApplicationRunner, ApplicationListener<ContextClosedEvent> {

    private String serverAddress;
    private Integer serverPort;
    private String token;
    private String proxyAddress;
    private Integer proxyPort;
    private Integer remotePort;
    private String clientName;

    private ChannelFuture channelFuture;

    @Override
    public void run(ApplicationArguments args) {
        EventLoopGroup eventExecutors = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventExecutors)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        socketChannel.pipeline().addLast(
                                new ProtobufVarint32FrameDecoder(),
                                new ProtobufDecoder(CrossLanMessage.getDefaultInstance()),
                                new ProtobufVarint32LengthFieldPrepender(),
                                new ProtobufEncoder(),
                                new IdleStateHandler(60, 30, 0),
                                new CrossLanClientHandler(token, remotePort, proxyAddress, proxyPort,clientName));
                    }
                });
        try {
            channelFuture = bootstrap.connect(serverAddress, serverPort).sync();
            channelFuture.channel().closeFuture().addListener(future -> eventExecutors.shutdownGracefully());
        } catch (InterruptedException e) {
            log.error("CrossLan client run failed",e);
            close();
        }
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        close();
    }

    public synchronized void close() {
        if (null != channelFuture) {
            channelFuture.channel().close();
        }
        log.info("CrossLan client closed.");
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setProxyAddress(String proxyAddress) {
        this.proxyAddress = proxyAddress;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void setRemotePort(Integer remotePort) {
        this.remotePort = remotePort;
    }

    public void setClientName(String clientName){
        this.clientName = clientName;
    }
}
