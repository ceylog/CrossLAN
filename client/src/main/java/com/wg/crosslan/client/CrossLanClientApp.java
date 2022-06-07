package com.wg.crosslan.client;

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

public class CrossLanClientApp {
    public static void main(String[] args) throws InterruptedException {
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
                                new CrossLanClientHandler("abcd1234",9999,"127.0.0.1",80));
                    }
                });
        try {
            ChannelFuture channelFuture = bootstrap.connect("localhost", 8088).sync();
            channelFuture.channel().closeFuture().addListener(future -> eventExecutors.shutdownGracefully());
        } catch (InterruptedException e) {
            eventExecutors.shutdownGracefully();
            throw e;
        }
    }
}
