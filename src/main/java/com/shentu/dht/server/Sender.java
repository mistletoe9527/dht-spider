package com.shentu.dht.server;

import com.shentu.dht.bcodec.Bencode;
import com.shentu.dht.peer.Node;
import com.shentu.dht.request.*;
import com.shentu.dht.util.DHTUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;


/**
 * Created by styb on 2019/3/13.
 */
@Data
@AllArgsConstructor
@Slf4j
public class Sender{


    private Map<Integer,Channel> channels;

    private Bencode bencode;

    public void add(int num,Channel channel){
        channels.put(num,channel);
    }

    public void findNode(String node,String target,InetSocketAddress address,int num){
        if(!channels.get(num).isWritable()){
            return;
        }
        FindNodeRequest findNodeRequest=new FindNodeRequest(node,target);
        channels.get(num).writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bencode.encode(DHTUtil.beanToMap(findNodeRequest))), address));
//        System.out.println(address.getAddress()+"="+address.getPort());
    }

    /**
     * 回复find_node回复
     */
    public  void findNodeReceive(String messageId,InetSocketAddress address, String nodeId, List<Node> nodeList,int num) {
        if(!channels.get(num).isWritable()){
            return;
        }
        FindNodeResponse findNodeResponse=new FindNodeResponse(messageId,nodeId,new String(Node.toBytes(nodeList), CharsetUtil.ISO_8859_1));
        channels.get(num).writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bencode.encode(DHTUtil.beanToMap(findNodeResponse))), address));
    }

    /**
     * 回复get_peers
     */
    public  void getPeersReceive(String messageId,InetSocketAddress address, String nodeId, String token, List<Node> nodeList,int num) {
        if(!channels.get(num).isWritable()){
            return;
        }
        GetPeersResponse getPeersResponse = new GetPeersResponse(messageId,nodeId, token, new String(Node.toBytes(nodeList), CharsetUtil.ISO_8859_1));
        channels.get(num).writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bencode.encode(DHTUtil.beanToMap(getPeersResponse))), address));
    }


    /**
     * 回复ping请求
     */
    public  void pingReceive(InetSocketAddress address, String nodeID,String messageId,int num) {
        if(!channels.get(num).isWritable()){
            return;
        }
        PingResponse pingResponse=new PingResponse(messageId,nodeID);
        channels.get(num).writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bencode.encode(DHTUtil.beanToMap(pingResponse))), address));
    }

    /**
     * 回复ping请求
     */
    public  void ping(InetSocketAddress address, String nodeID,int num) {
        if(!channels.get(num).isWritable()){
            return;
        }
        PingRequest pingRequest=new PingRequest(nodeID);
        channels.get(num).writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bencode.encode(DHTUtil.beanToMap(pingRequest))), address));
    }


    /**
     * 批量发送get_peers
     */
    public  void getPeersBatch(List<InetSocketAddress> addresses, String nodeId,String infoHash,String messageId,int num) {
        if(!channels.get(num).isWritable()){
            return;
        }
        GetPeersRequest request = new GetPeersRequest(messageId,nodeId, infoHash);
        for (InetSocketAddress address : addresses) {
            try {
//                log.info("bencode.encode(DHTUtil.beanToMap(request))"+bencode.encode(DHTUtil.beanToMap(request)));
                channels.get(num).writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bencode.encode(DHTUtil.beanToMap(request))), address));
            Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("发送GET_PEERS,失败.e:{}",e.getMessage());
            }
        }
    }


    /**
     * 回复announce_peer
     */
    public  void announcePeerReceive(String messageId,InetSocketAddress address, String nodeId,int num) {
        if(!channels.get(num).isWritable()){
            return;
        }
        AnnouncePeersResponse response = new AnnouncePeersResponse(messageId,nodeId);
        channels.get(num).writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bencode.encode(DHTUtil.beanToMap(response))), address));
    }

    /**
     * announce_peer
     */
    public  void announcePeer(String id,Integer impliedPort,String infoHash,Integer port,String token,InetSocketAddress address,int num) {
        if(!channels.get(num).isWritable()){
            return;
        }
        AnnouncePeersRequest announcePeersRequest = new AnnouncePeersRequest(id,impliedPort,infoHash,port,token);
        channels.get(num).writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bencode.encode(DHTUtil.beanToMap(announcePeersRequest))), address));
    }


}
