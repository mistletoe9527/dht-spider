package com.shentu.dht.client;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by styb on 2019/2/28.
 */
public class Test {

    private static DHTClient dhtClient;
    static {
        dhtClient = new DHTClient("127.0.0.1", 7000);
    }

    public static void main(String[] args) throws Exception{
        ExecutorService executorService = Executors.newFixedThreadPool(100);
//        byte[] nodeId = DHTUtil.buildNodeId();
//        byte[] hashInfo = DHTUtil.buildNodeId();
//        Map<String,List<Peer>> map=new HashMap<String,List<Peer>>();
        while(true)
        dhtClient.sendData("dfdf2389uehdfjksdhfdskfhadskfahfdsadjkfhsdfdkshf");
//        map.put(new String(hashInfo),new ArrayList<Peer>());
//        search(dhtClient,nodeId,hashInfo,map,0);
//        for(;;){
//            hashInfo = DHTUtil.buildNodeId();
//            if(map.get(new String(hashInfo))==null){
//                map.put(new String(hashInfo),new ArrayList<Peer>());
//            }
//            search(dhtClient,nodeId,hashInfo,map,0);
//        }

//        try{
//            for(;;){
//                executorService.execute(()->{
//                   byte[] hashInfo = DHTUtil.buildNodeId();
////                    if(map.get(new String(hashInfo))==null){
////                        map.put(new String(hashInfo),new ArrayList<Peer>());
////                    }
//                    search(dhtClient,nodeId,hashInfo,map,0);
//                });
//            }
//
//        }catch (Exception e){
//
//        }



    }
//    public static void search(DHTClient dhtClient,byte[] nodeId,byte[] hashInfo,Map<String,List<Peer>> map,int count){
//        if(count>10){
//            return;
//        }
//        try{
////            System.out.println(DHTUtil.bytesToHexString(nodeId).getBytes().length);
//            List<Peer> peerList = DHTUtil.getPeerList(dhtClient.getPeerNodeOnDHT2(new String(nodeId, CharsetUtil.ISO_8859_1), new String(hashInfo, CharsetUtil.ISO_8859_1)));
//            if(peerList!=null){
//                for(Peer p:peerList){
//                    if(p.getType()==1){
//                        map.get(hashInfo).add(p);
//                    }else{
//                        DHTClient peer = new DHTClient(p.getIp(), p.getPort());
//                        search(peer,nodeId,hashInfo,map,++count);
//                    }
//                }
//            }
//        }catch (Exception e){
//        }
//
//    }


}
