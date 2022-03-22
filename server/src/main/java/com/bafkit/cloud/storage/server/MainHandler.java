package com.bafkit.cloud.storage.server;

import com.bafkit.cloud.storage.server.services.AuthorizationService;
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
    private boolean uploadFlag = false;
    private boolean downloadFlag = false;
    private String msgSend;

    private long uploadFileSize;
    private long countBuffer;

    public MainHandler(AuthorizationService authorizationService, String root) {
        actionController = new ActionController(authorizationService, root);
        this.root = root;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected: " + ctx.channel().remoteAddress());
        channels.add(ctx.channel());
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf byteBuf = ((ByteBuf) msg).copy();
        try {
            if (!uploadFlag) {
                byte[] bytes = new byte[byteBuf.capacity()];
                for (int i = 0; i < byteBuf.capacity(); i++) {
                    bytes[i] = byteBuf.getByte(i);
                }
                String str = new String(bytes, 0, bytes.length, StandardCharsets.UTF_8);
                String[] parts = str.trim().split("\\s+");
                String cmd = parts[0];
                System.out.println(Arrays.toString(parts));

                switch (cmd) {
                    case ("auth"):
                        msgSend = actionController.authorization(parts);
                        break;
                    case ("reg"):
                        msgSend = actionController.registration(parts);
                        break;
                    case ("list"):
                        msgSend = actionController.list();
                        break;
                    case ("currentDir"):
                        msgSend = actionController.getCurrentDir();
                        break;
                    case ("cd"):
                        msgSend = actionController.cd(parts);
                        break;
                    case ("mkdir"):
                        msgSend = actionController.mkdir(parts);
                        break;
                    case ("upload"):
                        msgSend = actionController.upload(parts);
                        break;
                    case ("waitingSend"):
                        uploadFlag = true;
                        uploadFileSize = Long.parseLong(parts[1]);
                        msgSend = actionController.checkCapacity(parts[1]);
                        break;
                    case ("download"):
                        msgSend = actionController.download(parts);
                        break;
                    case ("waitingGet"):
                        downloadFlag = true;
                        break;
                    case ("copy"):
                        msgSend = actionController.copy(parts[1]);
                        break;
                    case ("paste"):
                        msgSend = actionController.paste();
                        break;
                    case ("cut"):
                        msgSend = actionController.cut(parts[1]);
                        break;
                    case ("delete"):
                        msgSend = actionController.delete(parts[1]);
                        break;
                    default:
                        msgSend = "unknown";
                        System.out.println("unknown command");
                        break;
                }

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
