package com.wg.crosslan.server.crosslan.handler;

import com.wg.crosslan.common.handler.CommonHandler;
import com.wg.crosslan.common.protocol.proto.CrossLanMessage;
import com.wg.crosslan.common.protocol.proto.Type;
import com.wg.crosslan.server.crosslan.server.ProxyServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CrossLANHandler extends CommonHandler {

    private final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private final ProxyServer proxyServer = new ProxyServer();

    private final String token;
    private boolean register;

    private int port;

    public CrossLANHandler(String token) {
        this.token = token;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("channelActive {}", ctx);
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.debug("channelRead ctx: {}\n msg -> {}", ctx, msg);
        CrossLanMessage cmsg = (CrossLanMessage) msg;
        Type type = cmsg.getType();
        if (Type.REGISTER == type) {
            processRegister(cmsg);
        } else if (register) {
            if (Type.DISCONNECTED == type) {
                processDisconnected(cmsg);
            } else if (Type.DATA == type) {
                processData(cmsg);
            } else if (Type.KEEPALIVE == type) {
                log.debug("keepalive .. {}",ctx.channel().remoteAddress());
            } else {
                log.error("unknow type:{}", type);
            }

        } else {
            ctx.close();
        }
    }

    private void processRegister(CrossLanMessage cmsg) {
        CrossLanMessage.Builder builder = CrossLanMessage.newBuilder();
        builder.setType(Type.REGISTER_RESULT);
        String token1 = cmsg.getMetaDataMap().get("token");
        if (null != token1 && !this.token.equals(token1)) {
            builder.setIsSuccess(false)
                    .putMetaData("message", "token is wrong");
        } else {
            int port = Integer.parseInt(cmsg.getMetaDataMap().get("remotePort"));
            CommonHandler handler = this;
            try {
                proxyServer.bind(port, new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        socketChannel.pipeline().addLast(
                                new ByteArrayDecoder(),
                                new ByteArrayEncoder(),
                                new ProxyHandler(handler));
                        channels.add(socketChannel);
                    }
                });
                builder.setIsSuccess(true)
                        .putMetaData("message", "mapping port:" + port);
                register = true;
                this.port = port;
                log.info("register success, mapping on port: {}", port);
            } catch (InterruptedException e) {
                log.error("start client proxy server error", e);
                builder.setIsSuccess(false)
                        .putMetaData("message", e.getMessage());
            }
        }
        ctx.writeAndFlush(builder.build());
        if (!register) {
            log.error("client register error");
            ctx.close();
        }
    }

    private void processDisconnected(CrossLanMessage cmsg) {
        channels.close(channel -> channel.id().asLongText().equals(cmsg.getChannelId()));
    }

    private void processData(CrossLanMessage cmsg) {
        channels.writeAndFlush(
                cmsg.getData().toByteArray(),
                channel -> channel.id().asLongText().equals(cmsg.getChannelId()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        proxyServer.close();
        if (register) {
            log.info("stop proxy server on port:{}", port);
        }
    }
}
