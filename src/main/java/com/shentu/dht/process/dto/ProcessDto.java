package com.shentu.dht.process.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * Created by styb on 2019/3/15.
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
public class ProcessDto {

    /**
     * 消息信息
     */
    private MessageInfo messageInfo;

    /**
     * 原始map
     */
    private Map<String,Object> rawMap;

    /**
     * 消息发送者
     */
    private InetSocketAddress sender;

    private int num;

}
