package com.shentu.dht.process;

import com.shentu.dht.peer.RoutingTable;
import com.shentu.dht.process.constant.MethodConstant;
import com.shentu.dht.process.dto.ProcessDto;
import com.shentu.dht.server.Sender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by styb on 2019/3/15.
 */
@Component(MethodConstant.PING)
@Slf4j
public class PingProcessor implements DHTProcess<ProcessDto>{

    @Autowired
    private RoutingTable routingTable;

    @Autowired
    private Sender sender;


    @Override
    public void activeProcess(ProcessDto processDto) {
        log.info("PingProcessor activeProcess "+processDto.getMessageInfo());
    }

    @Override
    public void passiveProcess(ProcessDto processDto) {
        log.info("PingProcessor passiveProcess " + processDto.getMessageInfo());
        this.sender.pingReceive(processDto.getSender(), routingTable.getNodeIdStr(),
                processDto.getMessageInfo().getMessageId());
    }



}
