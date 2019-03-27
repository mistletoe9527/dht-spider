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
import org.springframework.beans.factory.InitializingBean;

/**
 * Created by styb on 2019/3/12.
 */
@Getter
public class UDPServer implements InitializingBean{


    private TestHandler testHandler;

    private Config config;
    public UDPServer(TestHandler testHandler,Config config){
        this.testHandler=testHandler;
        this.config=config;
    }

    @SneakyThrows
    public void run() throws Exception {
       new Thread(()->{
           EventLoopGroup eventLoopGroup = null;
           try {
               eventLoopGroup = new NioEventLoopGroup();
               Bootstrap bootstrap = new Bootstrap();
               bootstrap.group(eventLoopGroup).channel(NioDatagramChannel.class)//通道类型也为UDP
                       .option(ChannelOption.SO_BROADCAST, true)//是广播,也就是UDP连接
                       .option(ChannelOption.SO_RCVBUF, 10000 * 1024)// 设置UDP读缓冲区为3M
                       .option(ChannelOption.SO_SNDBUF, 10000 * 1024)// 设置UDP写缓冲区为3M
                       .handler(testHandler);//配置的业务处理类
               bootstrap.bind(config.getPort()).sync().channel().closeFuture().await();
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
        run();
    }
}
