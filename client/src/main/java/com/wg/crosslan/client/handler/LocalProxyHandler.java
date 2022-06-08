package com.wg.crosslan.client.handler;

import com.google.protobuf.ByteString;
import com.wg.crosslan.common.handler.CommonHandler;
import com.wg.crosslan.common.protocol.proto.CrossLanMessage;
import com.wg.crosslan.common.protocol.proto.Type;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalProxyHandler extends CommonHandler {

    private final CommonHandler proxyHandler;

    private final String remoteChannelId;

    public LocalProxyHandler(CommonHandler proxyHandler, String remoteChannelId) {
        this.proxyHandler = proxyHandler;
        this.remoteChannelId = remoteChannelId;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.debug("channelRead {}",ctx);
        byte[] data = (byte[]) msg;
        CrossLanMessage dataMsg = CrossLanMessage.newBuilder()
                .setType(Type.DATA)
                .setChannelId(remoteChannelId)
                .setData(ByteString.copyFrom(data))
                .build();
        proxyHandler.getCtx().writeAndFlush(dataMsg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        CrossLanMessage disConnMsg = CrossLanMessage.newBuilder()
                .setType(Type.DISCONNECTED)
                .setChannelId(remoteChannelId)
                .build();
        proxyHandler.getCtx().writeAndFlush(disConnMsg);
    }
}
