package com.shentu.dht.server;

import com.shentu.dht.config.Config;
import com.shentu.dht.handler.TestHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by styb on 2019/3/12.
 */
@Getter
@Slf4j
public class UDPServer implements InitializingBean{


    private List<TestHandler> testHandlers=new ArrayList<>();

    private Config config;
    public UDPServer(List<TestHandler> testHandlers,Config config){
        this.testHandlers=testHandlers;
        this.config=config;
    }

    @SneakyThrows
    public void run(int num,int port) throws Exception {
       new Thread(()->{
           EventLoopGroup eventLoopGroup = null;
           try {
               eventLoopGroup = new NioEventLoopGroup();
               Bootstrap bootstrap = new Bootstrap();
               bootstrap.group(eventLoopGroup).channel(NioDatagramChannel.class)//通道类型也为UDP
                       .option(ChannelOption.SO_BROADCAST, true)//是广播,也就是UDP连接
                       .option(ChannelOption.SO_RCVBUF, 10000 * 1024)// 设置UDP读缓冲区为3M
                       .option(ChannelOption.SO_SNDBUF, 10000 * 1024)// 设置UDP写缓冲区为3M
                       .handler(testHandlers.get(num));//配置的业务处理类
               bootstrap.bind(port).sync().channel().closeFuture().await();
           }catch (Exception e){}
           finally{
               if (eventLoopGroup != null)
                   eventLoopGroup.shutdownGracefully();
           }
       }).start();
    }

    @SneakyThrows
    public static void main(String[] args) {
//        new UDPServer(6881).run();
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("UDPServer init ......");
        int port=config.getPort();
        for(int i=0;i<config.getThreadCount();i++){
            run(i,port);
            port++;
        }

    }
}
