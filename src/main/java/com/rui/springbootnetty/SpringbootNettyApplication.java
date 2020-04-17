package com.rui.springbootnetty;

import com.rui.springbootnetty.netty.NettyWebSocketServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringbootNettyApplication {

    private static NettyWebSocketServer server = new NettyWebSocketServer(8081);

    public static void main(String[] args) {
        SpringApplication.run(SpringbootNettyApplication.class, args);
        server.start();
    }

}
