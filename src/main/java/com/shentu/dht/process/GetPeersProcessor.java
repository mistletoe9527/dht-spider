package com.shentu.dht.process;

import com.alibaba.fastjson.JSONObject;
import com.shentu.dht.config.Config;
import com.shentu.dht.enmu.NodeRankEnum;
import com.shentu.dht.peer.Node;
import com.shentu.dht.peer.RoutingTable;
import com.shentu.dht.process.constant.MethodConstant;
import com.shentu.dht.process.dto.GetPeersSendInfo;
import com.shentu.dht.process.dto.MessageInfo;
import com.shentu.dht.process.dto.Peer;
import com.shentu.dht.process.dto.ProcessDto;
import com.shentu.dht.server.Sender;
import com.shentu.dht.task.FindMetaDataTask;
import com.shentu.dht.task.FindNodeTask;
import com.shentu.dht.util.DHTUtil;
import com.shentu.dht.util.FileUtil;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by styb on 2019/3/15.
 */
@Component(MethodConstant.GET_PEERS)
@Slf4j
public class GetPeersProcessor implements DHTProcess<ProcessDto>{

    @Autowired
    private FindNodeTask findNodeTask;

    @Autowired
    private List<RoutingTable> routingTables;

    @Autowired
    private Sender sender;
    @Resource
    private Map<String,GetPeersSendInfo> getPeersMap;
    @Autowired
    private Config config;
    @Autowired
    private FindMetaDataTask findMetaDataTask;

    private int maxPeers=1000;



    @Override
    public void activeProcess(ProcessDto processDto) {
        MessageInfo messageInfo = processDto.getMessageInfo();
        Map<String, Object> rawMap = processDto.getRawMap();
        InetSocketAddress sender = processDto.getSender();
        //查询缓存
        GetPeersSendInfo getPeersSendInfo = getPeersMap.get(messageInfo.getMessageId());
        //查询rMap,此处rMap不可能不存在
        Map<String, Object> rMap = DHTUtil.getParamMap(rawMap, "r", "");
        //缓存过期，则不做任何处理了
        if (getPeersSendInfo == null) return;
        if(getPeersSendInfo.getSentNodeIds().size()>maxPeers){
            getPeersMap.remove(messageInfo.getMessageId());
            return;
        }
        byte[] id = DHTUtil.getParamString(rMap, "id", "GET_PEERS-RECEIVE,找不到id参数.map:" + rMap).getBytes(CharsetUtil.ISO_8859_1);
        //如果返回的是nodes
        if (rMap.get("nodes") != null) {
            nodesHandler(messageInfo, sender, routingTables.get(processDto.getNum()), getPeersSendInfo, rMap, id,processDto.getNum());
            return;
        }

        if (rMap.get("values") == null) return ;
        //如果返回的是values peer
        valuesHandler(messageInfo, rawMap, sender, routingTables.get(processDto.getNum()), getPeersSendInfo, rMap, id,processDto.getNum());
    }

    @Override
    public void passiveProcess(ProcessDto processDto) {
        Map<String, Object> rawMap = processDto.getRawMap();
        InetSocketAddress sender = processDto.getSender();
        Map<String, Object> aMap = DHTUtil.getParamMap(rawMap, "a", "GET_PEERS,找不到a参数.map:" + rawMap);
        byte[] infoHash = DHTUtil.getParamString(aMap, "info_hash", "GET_PEERS,找不到info_hash参数.map:" + rawMap).getBytes(CharsetUtil.ISO_8859_1);
        byte[] id = DHTUtil.getParamString(aMap, "id", "GET_PEERS,找不到id参数.map:" + rawMap).getBytes(CharsetUtil.ISO_8859_1);
        List<Node> nodes = routingTables.get(processDto.getNum()).getForTop8(infoHash);
//                    log.info("{}GET_PEERS,发送者:{},info_hash:{}", LOG, sender,info_hash);
        //回复时,将自己的nodeId伪造为 和该节点异或值相差不大的值
        this.sender.getPeersReceive(processDto.getMessageInfo().getMessageId(), sender,
                DHTUtil.generateSimilarInfoHashString(id, 1),
                config.getToken(), nodes,processDto.getNum());
        //加入路由表
        routingTables.get(processDto.getNum()).put(new Node(id, sender, NodeRankEnum.GET_PEERS.getKey()));
    }


    /**
     * 处理values返回
     */
    private boolean valuesHandler(MessageInfo messageInfo, Map<String, Object> rawMap, InetSocketAddress sender, RoutingTable routingTable, GetPeersSendInfo getPeersSendInfo, Map<String, Object> rMap, byte[] id,int num) {
        log.info("valuesHandler "+ JSONObject.toJSONString(messageInfo));
        List<String> rawPeerList;
        try {
            rawPeerList = DHTUtil.getParamList(rMap, "values", "GET_PEERS-RECEIVE,找不到values参数.map:" + rawMap);
        } catch (Exception e) {
            //如果发生异常,说明该values参数可能是string类型的
            String values = DHTUtil.getParamString(rawMap, "values", "GET_PEERS-RECEIVE,找不到values参数.map:" + rawMap);
            rawPeerList = Collections.singletonList(values);
        }
        if (CollectionUtils.isEmpty(rawPeerList)) {
//			routingTable.delete(id);
            return true;
        }

        List<Peer> peerList = new LinkedList<>();
        for (String rawPeer : rawPeerList) {
            //byte[6] 转 Peer
            Peer peer = new Peer(rawPeer.getBytes(CharsetUtil.ISO_8859_1));
            peerList.add(peer);
        }
        //将peers连接为字符串
        final StringBuilder peersInfoBuilder = new StringBuilder();
        peerList.forEach(peer -> {
                peersInfoBuilder.append(peer.getIp()).append(":").append(peer.getPort()).append(":").append(num).append(";");
                findMetaDataTask.put(getPeersSendInfo.getInfoHash()+","+peer.getIp()+","+peer.getPort()+","+num);
            }
        );

//		log.info("{}发送者:{},info_hash:{},消息id:{},返回peers:{}", LOG, sender, getPeersSendInfo.getInfoHash(), messageInfo.getMessageId(), peersInfoBuilder.toString());
        //清除该任务缓存 和 连接peer任务
        getPeersMap.remove(messageInfo.getMessageId());

        //入库
        FileUtil.wirte("peers=="+getPeersSendInfo.getInfoHash()+"&"+peersInfoBuilder.toString()+"\r\n");

        //节点入库
//				nodeRepository.save(new Node(null, BTUtil.getIpBySender(sender), sender.getPort()));
        routingTable.put(new Node(id, sender, NodeRankEnum.GET_PEERS_RECEIVE_OF_VALUE.getKey()));
        //并向该节点发送findNode请求
        findNodeTask.put(sender);
        return true;
    }

    /**
     * 处理nodes返回
     * @param messageInfo 发送过来的消息信息
     * @param sender 发送者
     * @param routingTable 路由表
     * @param getPeersSendInfo 之前缓存的get_peers发送信息
     * @param rMap 消息的原始map
     * @param id 对方id
     */
    private boolean nodesHandler(MessageInfo messageInfo, InetSocketAddress sender, RoutingTable routingTable, GetPeersSendInfo getPeersSendInfo, Map<String, Object> rMap, byte[] id,int num) {
        log.info("nodesHandler "+ JSONObject.toJSONString(messageInfo));
        List<Node> nodeList = DHTUtil.getNodeListByRMap(rMap);
        if(CollectionUtils.isNotEmpty(nodeList)){
            for(Node node:nodeList){
                FileUtil.wirteNode("nodes2=="+node.getIp()+","+node.getPort()+"\r\n");
            }
        }
        //向新节点发送消息
        nodeList.forEach(item -> this.sender.findNode(routingTable.getNodeIdStr(), DHTUtil.generateNodeIdString(),item.toAddress(),num));
        //将消息发送者加入路由表.
        routingTable.put(new Node(id, sender, NodeRankEnum.GET_PEERS_RECEIVE.getKey()));
        //                    log.info("{}GET_PEERS-RECEIVE,发送者:{},info_hash:{},消息id:{},返回nodes", LOG, sender, getPeersSendInfo.getInfoHash(), messageInfo.getMessageId());

        //取出未发送过请求的节点

        List<Node> unSentNodeList = nodeList.stream().filter(node -> !getPeersSendInfo.contains(node.getNodeIdBytes())).collect(Collectors.toList());
        //为空退出
        if (CollectionUtils.isEmpty(unSentNodeList)) {
            log.info("发送者:{},info_hash:{},消息id:{},所有节点已经发送过请求.", sender, getPeersSendInfo.getInfoHash(), messageInfo.getMessageId());
            return true;
        }
        //未发送过请求的节点id
        List<byte[]> unSentNodeIdList = unSentNodeList.stream().map(Node::getNodeIdBytes).collect(Collectors.toList());
        //将其加入已发送队列
        getPeersSendInfo.put(unSentNodeIdList);
        //未发送过请求节点的地址
        List<InetSocketAddress> unSentAddressList = unSentNodeList.stream().map(Node::toAddress).collect(Collectors.toList());
        //批量发送请求
        this.sender.getPeersBatch(unSentAddressList, routingTable.getNodeIdStr(),
                new String(DHTUtil.hexStr2Bytes(getPeersSendInfo.getInfoHash()), CharsetUtil.ISO_8859_1),
                messageInfo.getMessageId(),num);
        return true;
    }
}
