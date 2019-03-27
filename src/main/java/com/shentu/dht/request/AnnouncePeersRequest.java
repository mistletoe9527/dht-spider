package com.shentu.dht.request;

import com.shentu.dht.bcodec.BTException;
import com.shentu.dht.enmu.MessageTypeEnmu;
import com.shentu.dht.enmu.MethodEnmu;
import com.shentu.dht.util.DHTUtil;
import io.netty.util.CharsetUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * Created by styb on 2019/3/20.
 */
public class AnnouncePeersRequest extends BaseRequest{
    private AnnounceRequestContent a=new AnnounceRequestContent();

    @Data
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class AnnounceRequestContent{
        private String id;
        private Integer implied_port;//可选 0/1 1会忽略port参数
        private String info_hash;
        private Integer port;
        private String token;
        public  AnnounceRequestContent (Map<String, Object> map,int defaultPort) {
            Map<String, Object> aMap = DHTUtil.getParamMap(map, "a", "ANNOUNCE_PEER,找不到a参数.map:" + map);
            info_hash = DHTUtil.bytes2HexStr(DHTUtil.getParamString(aMap, "info_hash", "ANNOUNCE_PEER,找不到info_hash参数.map:" + map)
                    .getBytes(CharsetUtil.ISO_8859_1));
            if (aMap.get("implied_port") == null || ((long) aMap.get("implied_port") )== 0) {
                Object portObj = aMap.get("port");
                if(portObj == null)
                    throw new BTException("ANNOUNCE_PEER,找不到port参数.map:" + map);
                port = ((Long) portObj).intValue();
            }else
                port = defaultPort;
            id = DHTUtil.bytes2HexStr(DHTUtil.getParamString(aMap, "id", "ANNOUNCE_PEER,找不到id参数.map:" + map).getBytes(CharsetUtil.ISO_8859_1));
        }
    }

    public AnnouncePeersRequest(String id,Integer impliedPort,String infoHash,Integer port,String token){
        super(DHTUtil.generateMessageId(),
                MessageTypeEnmu.QUERY.getKey(),
                MethodEnmu.ANNOUNCE_PEER.getKey());
        a.id=id;
        a.implied_port=impliedPort;
        a.info_hash=infoHash;
        a.port=port;
        a.token=token;

    }




    public static void main(String[] args) {
        FindNodeRequest f=new FindNodeRequest("df","23");
    }
}
