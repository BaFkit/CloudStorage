package com.bafkit.cloud.storage.server;

import com.bafkit.cloud.storage.server.services.AuthorizationService;
import com.bafkit.cloud.storage.server.services.CommandsService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


@Slf4j
public class MainHandler extends ChannelInboundHandlerAdapter {

    private final String root;
    private final ActionController actionController;
    private CommandsService commands;
    private boolean uploadFlag = false;
    private boolean downloadFlag = false;
    private String msgSend;
    private long uploadFileSize;
    private long countBuffer;
    private ByteBuf buf;

    public MainHandler(AuthorizationService authorizationService, String root) {
        actionController = new ActionController(authorizationService, root);
        this.root = root;
        commands = new CommandsService(actionController, this);
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
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        buf = ctx.alloc().buffer();
        buf = ((ByteBuf) msg).copy();
        try {
            if (!uploadFlag) {
                StringBuilder sb = new StringBuilder();
                while (buf.isReadable()) {
                    sb.append((char) buf.readByte());
                }
                String str = sb.toString();
                String[] parts = str.trim().split("\\s+");
                String cmd = parts[0];
                System.out.println(Arrays.toString(parts));

                msgSend = commands.executeCommand(cmd, parts);

                if (downloadFlag) {
                    byte[] bytesDownload = actionController.getBytes();
                    buf = Unpooled.copiedBuffer(bytesDownload);
                    downloadFlag = false;
                    ctx.writeAndFlush(buf);
                    buf.release();
                    return;
                }

                msg = Unpooled.copiedBuffer(msgSend.getBytes(StandardCharsets.UTF_8));
                ctx.writeAndFlush(msg);
                buf.release();
                return;
            }
            byte[] bytes = new byte[buf.capacity()];
            for (int i = 0; i < buf.capacity(); i++) {
                bytes[i] = buf.getByte(i);
            }
            countBuffer += buf.capacity();
            msgSend = actionController.uploadFile(bytes);
            if (uploadFileSize != countBuffer) return;
            uploadFlag = false;
            uploadFileSize = 0L;
            countBuffer = 0L;
            msg = Unpooled.copiedBuffer(msgSend.getBytes(StandardCharsets.UTF_8));
            ctx.writeAndFlush(msg);
        }finally {
            buf.release();
            buf = null;
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
