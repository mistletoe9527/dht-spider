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


/**
 * Created by styb on 2019/3/13.
 */
@Data
@AllArgsConstructor
@Slf4j
public class Sender{


    private Channel channel;

    private Bencode bencode;

    public void findNode(String node,String target,InetSocketAddress address){
        FindNodeRequest findNodeRequest=new FindNodeRequest(node,target);
        channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bencode.encode(DHTUtil.beanToMap(findNodeRequest))), address));
//        System.out.println(address.getAddress()+"="+address.getPort());
    }

    /**
     * 回复find_node回复
     */
    public  void findNodeReceive(String messageId,InetSocketAddress address, String nodeId, List<Node> nodeList) {
        FindNodeResponse findNodeResponse=new FindNodeResponse(messageId,nodeId,new String(Node.toBytes(nodeList), CharsetUtil.ISO_8859_1));
        channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bencode.encode(DHTUtil.beanToMap(findNodeResponse))), address));
    }

    /**
     * 回复get_peers
     */
    public  void getPeersReceive(String messageId,InetSocketAddress address, String nodeId, String token, List<Node> nodeList) {
        GetPeersResponse getPeersResponse = new GetPeersResponse(messageId,nodeId, token, new String(Node.toBytes(nodeList), CharsetUtil.ISO_8859_1));
        channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bencode.encode(DHTUtil.beanToMap(getPeersResponse))), address));
    }


    /**
     * 回复ping请求
     */
    public  void pingReceive(InetSocketAddress address, String nodeID,String messageId) {
        PingResponse pingResponse=new PingResponse(messageId,nodeID);
        channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bencode.encode(DHTUtil.beanToMap(pingResponse))), address));
    }

    /**
     * 回复ping请求
     */
    public  void ping(InetSocketAddress address, String nodeID) {
        PingRequest pingRequest=new PingRequest(nodeID);
        channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bencode.encode(DHTUtil.beanToMap(pingRequest))), address));
    }


    /**
     * 批量发送get_peers
     */
    public  void getPeersBatch(List<InetSocketAddress> addresses, String nodeId,String infoHash,String messageId) {
        GetPeersRequest request = new GetPeersRequest(messageId,nodeId, infoHash);
        for (InetSocketAddress address : addresses) {
            try {
                log.info("bencode.encode(DHTUtil.beanToMap(request))"+bencode.encode(DHTUtil.beanToMap(request)));
                channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bencode.encode(DHTUtil.beanToMap(request))), address));
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
    public  void announcePeerReceive(String messageId,InetSocketAddress address, String nodeId) {
        AnnouncePeersResponse response = new AnnouncePeersResponse(messageId,nodeId);
        channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bencode.encode(DHTUtil.beanToMap(response))), address));
    }

    /**
     * announce_peer
     */
    public  void announcePeer(String id,Integer impliedPort,String infoHash,Integer port,String token,InetSocketAddress address) {
        AnnouncePeersRequest announcePeersRequest = new AnnouncePeersRequest(id,impliedPort,infoHash,port,token);
        channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bencode.encode(DHTUtil.beanToMap(announcePeersRequest))), address));
    }


}
