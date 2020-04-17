package com.rui.springbootnetty.netty.golbal;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.log4j.Logger;

/**
 * @author xiaorui
 */
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = Logger.getLogger(this.getClass());

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                logger.debug("进入读空闲");
            }

            if (event.state() == IdleState.WRITER_IDLE) {
                logger.debug("进入写空闲");
            }
            if (event.state() == IdleState.ALL_IDLE) {
                // 关闭连接
                logger.debug("因为长时间未操作，关闭连接" + ctx.channel());
                Channel channel = ctx.channel();
                channel.close();
                ChannelSupervise.removeChannel(channel);
            }
        }
        super.userEventTriggered(ctx, evt);
    }
}
