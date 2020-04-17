package com.rui.springbootnetty.netty.golbal;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 对全局channel的一个管理
 * @author xiaorui
 */
public class ChannelSupervise {
    /**
     * 用来存储全局channel
     */
    private static ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 用来存储channelMap,String为uid
     * 为了实现私聊功能
     */
    private static ConcurrentHashMap<String, Channel> channelMap = new ConcurrentHashMap<>();
    /**
     * 增加channel
     * @param channel 需要增添的channel
     */
    public static void addChannel(Channel channel) {
        group.add(channel);
    }

    /**
     * 删除channel
     * @param channel 需要删除的channel
     */
    public static void removeChannel(Channel channel) {
        group.remove(channel);
    }

    public static void sendAll(String msg) {
        group.writeAndFlush(msg);
    }
}
