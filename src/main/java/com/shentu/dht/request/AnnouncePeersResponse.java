package com.shentu.dht.request;

import com.shentu.dht.enmu.MessageTypeEnmu;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by styb on 2019/3/11.
 */
@Data
public class AnnouncePeersResponse extends BaseResponse{
    private PingResponseContent r=new PingResponseContent();

    @Data
    @Accessors(chain = true)
    private static class PingResponseContent{
        private String id;
    }

    public AnnouncePeersResponse(String messageId, String id){
        super(messageId,
                MessageTypeEnmu.RESPONSE.getKey());
        r.id=id;
    }

    public static void main(String[] args) {
//        GetPeersRequest f=new GetPeersRequest("df","23");
//        System.out.println(JSONObject.toJSONString(f));


    }
}
