package com.shentu.dht.request;


import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by styb on 2019/3/12.
 */
@Data
@AllArgsConstructor()
public class BaseRequest {

    private String t;//messageid 2 byte

    private String y;//"q" for query, "r" for response, or "e" for error

    private String q;//method ping/find_node/get_peers/announce_peer

    //private String v;//client identifier registered in BEP 20 这里不管这个




}
