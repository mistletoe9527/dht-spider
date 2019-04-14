package com.shentu.dht.task;

import com.shentu.dht.config.Config;
import com.shentu.dht.peer.Node;
import com.shentu.dht.peer.RoutingTable;
import com.shentu.dht.process.dto.GetPeersSendInfo;
import com.shentu.dht.server.Sender;
import com.shentu.dht.util.DHTUtil;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * Created by styb on 2019/3/14.
 */
@Component
@Slf4j
public class FindNodeTask {


    private Sender sender;
    private Config config;
    private List<RoutingTable> routingTables;
    private Map<String,GetPeersSendInfo> getPeersMap;

    public FindNodeTask(Sender sender,List<RoutingTable> routingTables,Config config,Map<String,GetPeersSendInfo> getPeersMap){
        this.sender=sender;
        this.routingTables=routingTables;
        this.config=config;
        this.getPeersMap=getPeersMap;
    }

    private BlockingQueue<InetSocketAddress> blockingQueue=new LinkedBlockingQueue<>();


    public void put(InetSocketAddress address){
        blockingQueue.offer(address);
    }
    public static LinkedBlockingQueue<String> queue=new LinkedBlockingQueue();
    public void run() throws Exception{
        init();
        new Thread(()->{
            try{
                Thread.sleep(10000);
            }catch (Exception e){
            }
            for(;;){
                try{
                    InetSocketAddress take = blockingQueue.take();
                    for(int i=0;i<config.getThreadCount();i++){
                        sender.findNode(routingTables.get(i).getNodeIdStr(), DHTUtil.generateNodeIdString(),take,i);
                    }
                    Thread.sleep(500);
                }catch (Exception e){
                    e.printStackTrace();
                    log.error("error in FindNodeTask msg={}",e.getMessage());
                }
            }
        }).start();

        new Thread(
            ()->{
                try{
                    Thread.sleep(1000*60*5);
                }catch (Exception e){}
                for(;;){
                    try {
                        Thread.sleep(500);
                        String infoHashHexStr=queue.take();
                        for(int i=0;i<config.getThreadCount();i++){
                            //消息id
                            String messageId = DHTUtil.generateMessageId();
                            //		log.info("{}开始新任务.消息Id:{},infoHash:{}", LOG, messageId, infoHashHexStr);

                            //当前已发送节点id
                            List<byte[]> nodeIdList = new ArrayList<>();
                            //获取最近的8个地址
                            List<Node> nodeList = routingTables.get(i).getForTop8(DHTUtil.hexStr2Bytes(infoHashHexStr));
                            //目标nodeId
                            nodeIdList.addAll(nodeList.stream().map(Node::getNodeIdBytes).collect(Collectors.toList()));
                            //目标地址
                            List<InetSocketAddress> addresses = nodeList.stream().map(Node::toAddress).collect(Collectors.toList());
                            //存入缓存
                            getPeersMap.put(messageId, new GetPeersSendInfo(infoHashHexStr).put(nodeIdList));
                            //批量发送
                            this.sender.getPeersBatch(addresses, routingTables.get(i).getNodeIdStr(), new String(DHTUtil.hexStr2Bytes(infoHashHexStr), CharsetUtil.ISO_8859_1), messageId,i);
//                        this.sender.announcePeer(routingTable.getNodeIdStr(),0,new String(RandomUtils.nextBytes(20), CharsetUtil.ISO_8859_1),6881,"styb", new InetSocketAddress("127.0.0.1", 6881));
//                        Thread.sleep(10000);
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        )
        .start();

        new Thread(()->{
            try{
                Thread.sleep(1000*60*6);
                queue.offer("F4B8C2A18AADC2223805C97CC652A101D67722E9");
                queue.offer("7E8948E0179ABA8DFF5974E12A31766D9E8C9219");
                queue.offer("EF52237704213E9934CB4D8BDFFF21589A12CB41");
                queue.offer("EF52237704213E9934CB4D8BDFFF21589A12CB41");
                queue.offer("DEFF5E04FBB74FD3E620788730AF3931A6C9F38B");
                queue.offer("B6D48290E8B2E6DA4C745EC616BF8AA516A6630A");
                queue.offer("9A76F4494A339529AEF7CBBE981EED3796931AD7");
                queue.offer("09CC9CFDF96C90B33AC83F46F29C6FB5BDAC0E52");
                queue.offer("40B0EF821CDB5FB7383D1E2EFD5CB45923B5A776");
                for(;;){
                    queue.offer(DHTUtil.bytes2HexStr(RandomUtils.nextBytes(20)).toUpperCase());
                    queue.offer(DHTUtil.bytes2HexStr(RandomUtils.nextBytes(20)).toLowerCase());
                    Thread.sleep(1000);
                }
            }catch (Exception e){
            }
        }).start();
    }

    public void init(){
        config.getAddress().getSuperAddressList().stream().map( x->
                new InetSocketAddress(x.split(":")[0],Integer.parseInt(x.split(":")[1]))
        ).forEach(x-> put(x));
    }






}
