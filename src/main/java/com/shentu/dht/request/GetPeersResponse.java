package com.shentu.dht.request;

import com.shentu.dht.enmu.MessageTypeEnmu;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by styb on 2019/3/11.
 */
@Data
public class GetPeersResponse extends BaseResponse{
    private GetPeersResponseContent r=new GetPeersResponseContent();

    @Data
    @Accessors(chain = true)
    private static class GetPeersResponseContent{
        private String id;
        private String token;
        private String nodes;
    }

    public GetPeersResponse(String messageId, String id, String token,String nodes){
        super(messageId,
                MessageTypeEnmu.RESPONSE.getKey());
        r.id=id;
        r.token=token;
        r.nodes=nodes;
    }

    public static void main(String[] args) {
//        GetPeersRequest f=new GetPeersRequest("df","23");
//        System.out.println(JSONObject.toJSONString(f));

        GetPeersResponse getPeersResponse = new GetPeersResponse("mid","id","token","nodes");

    }
}
