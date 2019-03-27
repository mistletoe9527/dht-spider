package com.shentu.dht.process.dto;

import com.shentu.dht.enmu.MessageTypeEnmu;

import com.shentu.dht.enmu.MethodEnmu;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Created by styb on 2019/3/15.
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class MessageInfo {

    /**
     * 方法
     */
    private MethodEnmu method;

    /**
     * 状态
     */
    private MessageTypeEnmu status;

    /**
     * 消息id
     */
    private String messageId;
}
