package com.rui.springbootnetty.netty;

import com.rui.springbootnetty.netty.golbal.HeartBeatHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author xiaorui
 */
public class MyServerChannelHandlerInitial extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel cxt) throws Exception {
        ChannelPipeline pipeline = cxt.pipeline();
        // 基于http的协议，请求解码
        pipeline.addLast("http-codec", new HttpServerCodec());
        // 对httpMessage聚合，聚合成FullHttpRequest和FullHttpResponse
        pipeline.addLast("http-agg", new HttpObjectAggregator(1024 * 64));
        // 支持大数据的读写
        pipeline.addLast("http-chunk", new ChunkedWriteHandler());
        // 心态检查，超时断开连接
        pipeline.addLast("idle", new IdleStateHandler(60, 60, 120));
        pipeline.addLast("heartBeat", new HeartBeatHandler());
        pipeline.addLast("myHandler", new MyChannelInBoundHandler());

    }
}
