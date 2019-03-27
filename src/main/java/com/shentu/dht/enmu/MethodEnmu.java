package com.shentu.dht.enmu;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by styb on 2019/3/12.
 */
@AllArgsConstructor
@Getter
public enum MethodEnmu implements CodeEnum<String>{

    PING("ping","心跳检查"),
    FIND_NODE("find_node","查找NODE"),
    GET_PEERS("get_peers","通过INFO-HASH查找peer"),
    ANNOUNCE_PEER("announce_peer","宣布自己也有infohash");

    private  String key;
    private  String description;

}
