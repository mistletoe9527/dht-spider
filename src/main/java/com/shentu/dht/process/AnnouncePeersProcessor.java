package com.shentu.dht.process;

import com.alibaba.fastjson.JSONObject;
import com.shentu.dht.enmu.NodeRankEnum;
import com.shentu.dht.peer.Node;
import com.shentu.dht.peer.RoutingTable;
import com.shentu.dht.process.constant.MethodConstant;
import com.shentu.dht.process.dto.ProcessDto;
import com.shentu.dht.request.AnnouncePeersRequest;
import com.shentu.dht.server.Sender;
import com.shentu.dht.task.FindMetaDataTask;
import com.shentu.dht.task.FindNodeTask;
import com.shentu.dht.util.DHTUtil;
import com.shentu.dht.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by styb on 2019/3/20.
 */
@Slf4j
@Component(MethodConstant.ANNOUNCE_PEER)
public class AnnouncePeersProcessor implements DHTProcess<ProcessDto>{

    private String LOG="AnnouncePeersProcessor ";
    @Autowired
    private Sender sender;
    @Autowired
    private List<RoutingTable> routingTables;
    @Autowired
    private FindNodeTask findNodeTask;
    @Autowired
    private FindMetaDataTask findMetaDataTask;

    @Override
    public void activeProcess(ProcessDto processDto) {
        Map<String, Object> aMap = DHTUtil.getParamMap(processDto.getRawMap(), "r", "ANNOUNCE_PEER,找不到r参数.map:" + processDto.getRawMap());
        log.info("{}收到消息2.{}",LOG,aMap);
    }

    @Override
    public void passiveProcess(ProcessDto processDto) {
        AnnouncePeersRequest.AnnounceRequestContent requestContent = new AnnouncePeersRequest.AnnounceRequestContent(processDto.getRawMap(), processDto.getSender().getPort());
        log.info("{}收到消息1.",LOG+ JSONObject.toJSONString(requestContent));
        FileUtil.wirte("peers=="+requestContent.getInfo_hash() + "," + DHTUtil.getIpBySender(processDto.getSender()) + ":" + requestContent.getPort()+":"+processDto.getNum() + ";\r\n");
        //回复
        this.sender.announcePeerReceive(processDto.getMessageInfo().getMessageId(), processDto.getSender(), routingTables.get(processDto.getNum()).getNodeIdStr(),processDto.getNum());
        Node node = new Node(DHTUtil.hexStr2Bytes(requestContent.getId()), processDto.getSender(), NodeRankEnum.ANNOUNCE_PEER.getKey());
        //加入路由表
        routingTables.get(processDto.getNum()).put(node);

        //加入findNode任务队列
        findNodeTask.put(processDto.getSender());
        findMetaDataTask.put(requestContent.getInfo_hash() + "," + DHTUtil.getIpBySender(processDto.getSender()) + "," + requestContent.getPort()+","+processDto.getNum());
    }
}
