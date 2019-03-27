package com.shentu.dht.config;

import com.shentu.dht.bcodec.Bencode;
import com.shentu.dht.handler.TestHandler;
import com.shentu.dht.peer.Node;
import com.shentu.dht.peer.RoutingTable;
import com.shentu.dht.process.ProcessManager;
import com.shentu.dht.process.dto.GetPeersSendInfo;
import com.shentu.dht.server.Sender;
import com.shentu.dht.server.UDPServer;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

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
        return new Sender(null,bencode);
    }
    @Bean
    public ProcessManager processManager(){
        return new ProcessManager();
    }



    @Bean
    public TestHandler testHandler(Sender sender,Bencode bencode,ProcessManager processManager){
        return new TestHandler(sender,bencode,processManager);
    }
    @Bean
    public UDPServer udpServer(TestHandler testHandler,Config config){
        return new UDPServer(testHandler,config);
    }

    @Bean
    public RoutingTable routingTable(){
        RoutingTable routingTable = new RoutingTable();
        Map<Integer, PriorityQueue<Node>> tableMap = routingTable.getTableMap();
        tableMap.put(0,new PriorityQueue<>());
//        for(int i=0;i<1000;i++)
//        routingTable.put(new Node(RandomUtils.nextBytes(26)));
        return routingTable;
    }

    @Bean
    public Map<String,GetPeersSendInfo> getPeersMap(){
        return new HashMap<>();
    }






}
