package com.shentu.dht.process;

import com.shentu.dht.enmu.NodeRankEnum;
import com.shentu.dht.peer.Node;
import com.shentu.dht.peer.RoutingTable;
import com.shentu.dht.process.constant.MethodConstant;
import com.shentu.dht.process.dto.ProcessDto;
import com.shentu.dht.server.Sender;
import com.shentu.dht.task.FindNodeTask;
import com.shentu.dht.util.DHTUtil;
import com.shentu.dht.util.FileUtil;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by styb on 2019/3/15.
 */
@Component(MethodConstant.FIND_NODE)
@Slf4j
public class FindNodeProcessor implements DHTProcess<ProcessDto>{

    @Autowired
    private FindNodeTask findNodeTask;

    @Autowired
    private List<RoutingTable> routingTables;

    @Autowired
    private Sender sender;


    @Override
    public void activeProcess(ProcessDto processDto) {
        log.info("FindNodeProcessor activeProcess "+processDto.getMessageInfo()+"talbesize="+routingTables.size());
        Map<String, Object> rMap = DHTUtil.getParamMap(processDto.getRawMap(), "r", "FIND_NODE,找不到r参数.map:" + processDto.getRawMap());
        List<Node> nodeList = DHTUtil.getNodeListByRMap(rMap);
        //为空退出
        if (CollectionUtils.isEmpty(nodeList)) return;
        //去重
        Node[] nodes = nodeList.stream().distinct().toArray(Node[]::new);
        //将nodes加入发送队列
        for (Node node : nodes) {
            FileUtil.wirteNode("nodes1=="+node.getIp()+","+node.getPort()+"\r\n");
            findNodeTask.put(node.toAddress());
        }
        byte[] id = DHTUtil.getParamString(rMap, "id", "FIND_NODE,找不到id参数.map:" + processDto.getRawMap()).getBytes(CharsetUtil.ISO_8859_1);
        //将发送消息的节点加入路由表
        routingTables.get(processDto.getNum()).put(new Node(id, processDto.getSender(), NodeRankEnum.FIND_NODE_RECEIVE.getKey()));

    }

    @Override
    public void passiveProcess(ProcessDto processDto) {
        log.info("FindNodeProcessor passiveProcess "+processDto.getMessageInfo());
        //截取出要查找的目标nodeId和 请求发送方nodeId
        Map<String, Object> aMap = DHTUtil.getParamMap(processDto.getRawMap(), "a", "FIND_NODE,找不到a参数.map:" + processDto.getRawMap());
        byte[] targetNodeId = DHTUtil.getParamString(aMap, "target", "FIND_NODE,找不到target参数.map:" + processDto.getRawMap())
                .getBytes(CharsetUtil.ISO_8859_1);
        byte[] id = DHTUtil.getParamString(aMap, "id", "FIND_NODE,找不到id参数.map:" + processDto.getRawMap()).getBytes(CharsetUtil.ISO_8859_1);
        //查找
        List<Node> nodes = routingTables.get(processDto.getNum()).getForTop8(targetNodeId);
        this.sender.findNodeReceive(processDto.getMessageInfo().getMessageId(), processDto.getSender(),
                routingTables.get(processDto.getNum()).getNodeIdStr(),nodes,processDto.getNum());
        //操作路由表
        routingTables.get(processDto.getNum()).put(new Node(id, processDto.getSender(), NodeRankEnum.FIND_NODE.getKey()));
    }
}
