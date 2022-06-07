package com.wg.crosslan.client.handler;

import com.wg.crosslan.common.handler.CommonHandler;
import com.wg.crosslan.common.protocol.proto.CrossLanMessage;
import com.wg.crosslan.common.protocol.proto.Type;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CrossLanClientHandler extends CommonHandler {
    private Integer remotePort;
    private String token;
    private String proxyAddress;
    private Integer proxyPort;

    private ConcurrentHashMap<String, CommonHandler> channelHandlerMap = new ConcurrentHashMap<>();
    private ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public CrossLanClientHandler(String token, Integer remotePort, String proxyAddress, Integer proxyPort) {

        this.token = token;
        this.remotePort = remotePort;
        this.proxyAddress = proxyAddress;
        this.proxyPort = proxyPort;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        CrossLanMessage msg = CrossLanMessage.newBuilder()
                .setType(Type.REGISTER)
                .putMetaData("token", token)
                .putMetaData("remotePort", remotePort + "")
                .build();
        ctx.channel().writeAndFlush(msg);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.warn("channelInactive..  ");
        channels.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException {
        log.debug("channelRead \n" + msg);
        CrossLanMessage clmsg = (CrossLanMessage) msg;
        Type type = clmsg.getType();
        if (Type.REGISTER_RESULT == type) {
            handleRegisterResult(clmsg);
        } else if (Type.CONNECTED == type) {
            handleConnected(clmsg);
        } else if (Type.DISCONNECTED == type) {
            handleDisconnected(clmsg);
        } else if (Type.DATA == type) {
            handleData(clmsg);
        } else if (Type.KEEPALIVE == type) {
            log.info("keepalive ..");
        } else {
            throw new RuntimeException("unknow type: " + type);
        }
    }

    private void handleData(CrossLanMessage msg) {
        String channelId = msg.getChannelId();
        CommonHandler handler = channelHandlerMap.get(channelId);
        if (null != handler) {
            handler.getCtx().writeAndFlush(msg.getData().toByteArray());
        }
    }

    private void handleDisconnected(CrossLanMessage msg) {

        String channelId = msg.getChannelId();
        CommonHandler handler = channelHandlerMap.get(channelId);
        if (null != handler) {
            handler.getCtx().close();
            channelHandlerMap.remove(channelId);
        }
    }

    private void handleConnected(CrossLanMessage msg) throws InterruptedException {
        CommonHandler handler = this;
        String channelId = msg.getChannelId();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(workGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {

                        LocalProxyHandler localProxyHandler = new LocalProxyHandler(handler, channelId);
                        channel.pipeline().addLast(
                                new ByteArrayDecoder(),
                                new ByteArrayEncoder(),
                                localProxyHandler);
                        channelHandlerMap.put(channelId, localProxyHandler);
                        channels.add(channel);
                    }
                });
        try {
            Channel channel = b.connect(proxyAddress, proxyPort).sync().channel();
            channel.closeFuture().addListener(future -> workGroup.shutdownGracefully());
        } catch (InterruptedException e) {
            workGroup.shutdownGracefully();
            CrossLanMessage disConnMsg = CrossLanMessage.newBuilder()
                    .setType(Type.DISCONNECTED)
                    .setChannelId(channelId)
                    .build();
            ctx.writeAndFlush(disConnMsg);
            channelHandlerMap.remove(channelId);
            throw e;
        }


    }

    private void handleRegisterResult(CrossLanMessage msg) {
        if (msg.getIsSuccess()) {
            log.info("register success");
        } else {
            log.error("register fail:{}", msg.getMetaDataMap().get("message"));
            System.exit(-1);
        }
    }
}
