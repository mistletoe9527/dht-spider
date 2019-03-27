package com.shentu.dht.request;


import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by styb on 2019/3/12.
 */
@Data
@AllArgsConstructor()
public class BaseResponse {

    private String t;//messageid 2 byte

    private String y;//"q" for query, "r" for response, or "e" for error





}
