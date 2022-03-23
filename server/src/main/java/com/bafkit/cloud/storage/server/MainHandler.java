package com.bafkit.cloud.storage.server;

import com.bafkit.cloud.storage.server.services.AuthorizationService;
import com.bafkit.cloud.storage.server.services.CommandsService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class MainHandler extends ChannelInboundHandlerAdapter {

    private static final List<Channel> channels = new ArrayList<>();
    private final String root;

    private final ActionController actionController;
    private CommandsService commands;
    private boolean uploadFlag = false;
    private boolean downloadFlag = false;
    private String msgSend;
    private long uploadFileSize;
    private long countBuffer;
    public MainHandler(AuthorizationService authorizationService, String root) {
        actionController = new ActionController(authorizationService, root);
        this.root = root;
    }

    public void setUploadFlag(boolean uploadFlag) {
        this.uploadFlag = uploadFlag;
    }
    public void setDownloadFlag(boolean downloadFlag) {
        this.downloadFlag = downloadFlag;
    }
    public void setUploadFileSize(long uploadFileSize) {
        this.uploadFileSize = uploadFileSize;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected: " + ctx.channel().remoteAddress());
        channels.add(ctx.channel());
        commands = new CommandsService(actionController, this);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf byteBuf = ((ByteBuf) msg).copy();
        try {
            if (!uploadFlag) {
                StringBuilder sb = new StringBuilder();
                while (byteBuf.isReadable()) {
                    sb.append((char) byteBuf.readByte());
                }
                String str = sb.toString();
                String[] parts = str.trim().split("\\s+");
                String cmd = parts[0];
                System.out.println(Arrays.toString(parts));

                msgSend = commands.executeCommand(cmd, parts);

                if (downloadFlag) {
                    byte[] bytesDownload = actionController.getBytes();
                    byteBuf = Unpooled.copiedBuffer(bytesDownload);
                    downloadFlag = false;
                    ctx.writeAndFlush(byteBuf);
                    byteBuf.clear();
                    return;
                }

                msg = Unpooled.copiedBuffer(msgSend.getBytes(StandardCharsets.UTF_8));
                ctx.writeAndFlush(msg);
                byteBuf.clear();
                return;
            }
            byte[] bytes = new byte[byteBuf.capacity()];
            for (int i = 0; i < byteBuf.capacity(); i++) {
                bytes[i] = byteBuf.getByte(i);
            }
            countBuffer += byteBuf.capacity();
            msgSend = actionController.uploadFile(bytes);
            if (uploadFileSize != countBuffer) return;
            uploadFlag = false;
            uploadFileSize = 0L;
            countBuffer = 0L;
            msg = Unpooled.copiedBuffer(msgSend.getBytes(StandardCharsets.UTF_8));
            ctx.writeAndFlush(msg);
        }finally {
            byteBuf.release();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client disconnected: " + ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage(), cause);
    }
}
