package com.shentu.dht.task;

import com.shentu.dht.bcodec.Bencode;
import com.shentu.dht.client.ESClient;
import com.shentu.dht.handler.SearchMetaDataHandler;
import com.shentu.dht.peer.RoutingTable;
import com.shentu.dht.util.FileUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by styb on 2019/3/14.
 */
@Component
@Slf4j
public class FindMetaDataTask {

    private List<RoutingTable> routingTables;
    private Set<String> executedPeers=new HashSet<>();
    
    private Bencode bencode;

    private Bootstrap bootstrap;

    private ESClient esClient;


    public FindMetaDataTask(List<RoutingTable> routingTables, Bencode bencode,Bootstrap bootstrap,ESClient esClient){
        this.routingTables=routingTables;
        this.bencode=bencode;
        this.bootstrap=bootstrap;
        this.esClient=esClient;
    }

    public BlockingQueue<String> blockingQueue=new LinkedBlockingQueue<>();


    public void put(String val){
        if(!executedPeers.contains(val)){
            executedPeers.add(val);
            blockingQueue.add(val);
        }
    }


    @SneakyThrows
    public void run() throws Exception{
        init();
        new Thread(()->{

            for(;;){
                try{
                    String take = blockingQueue.take();
                    String[] split = take.split(",");
                    final int num=Integer.parseInt(split[3]);
                    log.info("FindMetaDataTask start="+take);
                    bootstrap.clone().handler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new ReadTimeoutHandler(15))
                                    .addLast(new SearchMetaDataHandler(bencode, routingTables.get(num),esClient, split[0]));

                        }
                    }).connect(new InetSocketAddress(split[1], Integer.parseInt(split[2])));
                    Thread.sleep(50);
                }catch (Exception e){
                }

            }

        }).start();
    }

    public void init(){
        new Thread(()->{
            List<String> infoHashPeer = FileUtil.getInfoHashPeer();
            log.info("infoHashPeersize=="+infoHashPeer.size());
            for(String s:infoHashPeer){
                if(!executedPeers.contains(s)){
                    executedPeers.add(s);
                    blockingQueue.add(s);
                }
            }
        }).start();
    }

}
