package com.shentu.dht.request;

import com.shentu.dht.enmu.MessageTypeEnmu;
import com.shentu.dht.enmu.MethodEnmu;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by styb on 2019/3/11.
 */
@Data
public class GetPeersRequest extends BaseRequest{
    private GetPeersRequestContent a=new GetPeersRequestContent();

    @Data
    @Accessors(chain = true)
    private static class GetPeersRequestContent{
        private String id;
        private String info_hash;
    }

    public GetPeersRequest(String messageId,String id, String infoHash){
        super(messageId,
                MessageTypeEnmu.QUERY.getKey(),
                MethodEnmu.GET_PEERS.getKey());
        a.id=id;
        a.info_hash=infoHash;
    }

    public static void main(String[] args) {
//        GetPeersRequest f=new GetPeersRequest("df","23");
//        System.out.println(JSONObject.toJSONString(f));
    }
}
