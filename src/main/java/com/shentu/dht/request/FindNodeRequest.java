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
public class FindNodeRequest extends BaseRequest{
    private FindNodeRequestContent a=new FindNodeRequestContent();

    @Data
    @Accessors(chain = true)
    private static class FindNodeRequestContent{
        private String id;
        private String target;
    }

    public FindNodeRequest(String id,String target){
        super(DHTUtil.generateMessageId(),
                MessageTypeEnmu.QUERY.getKey(),
                MethodEnmu.FIND_NODE.getKey());
        a.id=id;
        a.target=target;
    }

    public static void main(String[] args) {
        FindNodeRequest f=new FindNodeRequest("df","23");
    }
}
