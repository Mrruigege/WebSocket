package com.rui.springbootnetty.netty;

import com.rui.springbootnetty.netty.golbal.ChannelSupervise;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.apache.log4j.Logger;

import static io.netty.handler.codec.http.HttpUtil.isKeepAlive;

/**
 * 自定义消息处理类
 * @author xiaorui
 */
public class MyChannelInBoundHandler extends SimpleChannelInboundHandler<Object> {

    private final Logger logger = Logger.getLogger(this.getClass());

    private WebSocketServerHandshaker serverHandshaker = null;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.info("收到消息" + msg);
        if (msg instanceof FullHttpRequest) {
            // 以http接入，处理websocket握手
            handleHttpRequest(ctx, (FullHttpRequest)msg);
        } else if (msg instanceof WebSocketFrame) {
            // 处理websocket消息
            handleWebSocketFrame(ctx, (WebSocketFrame)msg);
        }
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("有新用户加入连接....");
        ChannelSupervise.addChannel(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("用户断开连接....");
        ChannelSupervise.removeChannel(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("出现异常" + ctx.channel());
        cause.printStackTrace();
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        Channel channel = ctx.channel();
        if (frame instanceof CloseWebSocketFrame) {
            // 关闭的指令
            serverHandshaker.close(channel, (CloseWebSocketFrame) frame.retain());
            return;
        }

        if (frame instanceof PingWebSocketFrame) {
            // 如果是ping信息
            channel.write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        if (frame instanceof BinaryWebSocketFrame) {
            logger.debug("不支持的字节数据");
            throw new UnsupportedOperationException(String.format(
                    "%s frame types not supported", frame.getClass().getName()));
        }

        String request = ((TextWebSocketFrame)frame).text().toString();
        logger.debug("服务端收到消息" + request);
        ChannelSupervise.sendAll(request);
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {

        if (!req.decoderResult().isSuccess() ||
                !"websocket".equals(req.headers().get("Upgrade"))) {
        // 如果对请求消息解码失败或者不是websocket请求，发送失败给客户端
            sendHttpResponse(ctx, req, new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }
        WebSocketServerHandshakerFactory wsfactory = new WebSocketServerHandshakerFactory("ws://localhost:8081/websocket", null, false);
        serverHandshaker = wsfactory.newHandshaker(req);
        if (serverHandshaker == null) {
            // 握手失败
            logger.info("握手失败");
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            // 进行握手
            serverHandshaker.handshake(ctx.channel(), req);
        }

    }

    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultHttpResponse res) {

        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(),
                    CharsetUtil.UTF_8);
            req.content().writeBytes(buf);
            buf.release();
        }
        ChannelFuture future = ctx.channel().writeAndFlush(res);
        // 如果是非keepAlive, 关闭连接
        if (isKeepAlive(req) || res.status().code() != 200) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
