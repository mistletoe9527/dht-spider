package com.shentu.dht.enmu;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by styb on 2019/3/12.
 */

@Getter
@AllArgsConstructor
public enum MessageTypeEnmu implements CodeEnum<String>{

    QUERY("q","查询"),
    RESPONSE("r","返回信息"),
    ERROR("e","错误信息");
    private String key;
    private String description;

}
