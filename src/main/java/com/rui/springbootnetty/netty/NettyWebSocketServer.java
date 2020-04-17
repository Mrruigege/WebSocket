package com.rui.springbootnetty.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;

/**
 * netty服务器
 * @author xiaorui
 */
public class NettyWebSocketServer {
    private final Logger logger=Logger.getLogger(this.getClass());
    /**
     * 处理用户连接请求的线程组
     */
    private NioEventLoopGroup boss = null;
    /**
     * 处理以连接的用户组
     */
    private NioEventLoopGroup work = null;

    private ServerBootstrap server = null;

    private int port;


    public NettyWebSocketServer(int port) {
        boss = new NioEventLoopGroup(1);
        work = new NioEventLoopGroup(5);
        server = new ServerBootstrap();
        this.port = port;
    }

    public void start() {
        try {
            server.group(boss, work)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new MyServerChannelHandlerInitial());
            ChannelFuture future = server.bind(port).sync();
            logger.info(port);
            logger.info("Netty服务器启动成功....");
            // 这里会无限循环
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.info("Netty服务器运行出错....");
            if (boss != null) {
                boss.shutdownGracefully();
            }
            if (work != null) {
                work.shutdownGracefully();
            }
        } finally {
            logger.info("Netty服务器关闭");
            boss.shutdownGracefully();
            work.shutdownGracefully();
        }
    }
}
