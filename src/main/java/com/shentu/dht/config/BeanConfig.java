package com.shentu.dht.config;

import com.shentu.dht.bcodec.Bencode;
import com.shentu.dht.handler.TestHandler;
import com.shentu.dht.peer.Node;
import com.shentu.dht.peer.RoutingTable;
import com.shentu.dht.process.ProcessManager;
import com.shentu.dht.process.dto.GetPeersSendInfo;
import com.shentu.dht.server.Sender;
import com.shentu.dht.server.UDPServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by styb on 2019/3/12.
 */
@Configuration
@Data
public class BeanConfig {

    @Bean
    public Bencode bencode(){
        return new Bencode();
    }

    @Bean
    public Sender sender(Bencode bencode){
        return new Sender(new ConcurrentHashMap<>(),bencode);
    }
    @Bean
    public ProcessManager processManager(){
        return new ProcessManager();
    }


    @Bean
    public List<TestHandler> testHandlers(Config config,Sender sender,Bencode bencode,ProcessManager processManager){
        List<TestHandler> list=new ArrayList<>();
        for(int i=0;i<config.getThreadCount();i++){
            list.add(new TestHandler(sender,bencode,processManager,i));
        }
        return list;
    }


    @Bean
    public UDPServer udpServer(List<TestHandler> testHandlers,Config config){
        return new UDPServer(testHandlers,config);
    }

    @Bean
    public List<RoutingTable> routingTableList(Config config){
        List<RoutingTable> list=new ArrayList<>();
        for(int i=0;i<config.getThreadCount();i++){
            RoutingTable routingTable = new RoutingTable();
            Map<Integer, PriorityQueue<Node>> tableMap = routingTable.getTableMap();
            tableMap.put(0,new PriorityQueue<>());
            list.add(routingTable);
        }
//        for(int i=0;i<1000;i++)
//        routingTable.put(new Node(RandomUtils.nextBytes(26)));
        return list;
    }

    @Bean
    public Map<String,GetPeersSendInfo> getPeersMap(){
        return new HashMap<>();
    }

    @Bean
    public Bootstrap bootstrap(){
       return new Bootstrap() // (1)
        .group(new NioEventLoopGroup()) // (2)
        .channel(NioSocketChannel.class) // (3)
        .option(ChannelOption.SO_KEEPALIVE, true) // (4)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 4000)
        .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(1, 102400, Integer.MAX_VALUE));
    }






}
