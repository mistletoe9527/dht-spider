package com.shentu.dht.request;

import com.shentu.dht.enmu.MessageTypeEnmu;
import com.shentu.dht.enmu.MethodEnmu;
import com.shentu.dht.util.DHTUtil;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by styb on 2019/3/11.
 */
@Data
public class PingRequest extends BaseRequest{
    private PingRequestContent a=new PingRequestContent();

    @Data
    @Accessors(chain = true)
    private static class PingRequestContent{
        private String id;
    }

    public PingRequest(String id) {
        super(DHTUtil.generateMessageId(),
                MessageTypeEnmu.QUERY.getKey(),
                MethodEnmu.PING.getKey());
        a.id = id;
    }

    public static void main(String[] args) {

    }
}
