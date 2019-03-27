package com.shentu.dht.request;

import com.shentu.dht.enmu.MessageTypeEnmu;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by styb on 2019/3/11.
 */
@Data
public class FindNodeResponse extends BaseResponse{

    private FindNodeRequestContent r=new FindNodeRequestContent();

    @Data
    @Accessors(chain = true)
    private static class FindNodeRequestContent{
        private String id;
        private String nodes;
    }

    public FindNodeResponse(String messageId,String id,String nodes){
        super(messageId, MessageTypeEnmu.RESPONSE.getKey());
        r.id=id;
        r.nodes=nodes;
    }



}
