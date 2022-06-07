package com.wg.crosslan.server.crosslan.handler;

import com.google.protobuf.ByteString;
import com.wg.crosslan.common.handler.CommonHandler;
import com.wg.crosslan.common.protocol.proto.CrossLanMessage;
import com.wg.crosslan.common.protocol.proto.Type;
import io.netty.channel.ChannelHandlerContext;

public class ProxyHandler extends CommonHandler {

    private CommonHandler proxyHandler;

    public ProxyHandler(CommonHandler proxyHandler){
        this.proxyHandler = proxyHandler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        CrossLanMessage connMsg = CrossLanMessage.newBuilder()
                .setChannelId(ctx.channel().id().asLongText())
                .setType(Type.CONNECTED)
                .build();
        proxyHandler.getCtx().writeAndFlush(connMsg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        CrossLanMessage disConnMsg = CrossLanMessage.newBuilder()
                .setType(Type.DISCONNECTED)
                .setChannelId(ctx.channel().id().asLongText())
                .build();
        proxyHandler.getCtx().writeAndFlush(disConnMsg);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        byte[] data = (byte[]) msg;
        CrossLanMessage dataMsg = CrossLanMessage.newBuilder()
                .setType(Type.DATA)
                .setChannelId(ctx.channel().id().asLongText())
                .setData(ByteString.copyFrom(data))
                .build();
        proxyHandler.getCtx().writeAndFlush(dataMsg);
    }
}
