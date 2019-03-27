package com.shentu.dht.peer;

import io.netty.util.CharsetUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.BeanUtils;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by styb on 2019/3/13.
 */
@Data
public class RoutingTable {

    private byte[] nodeId= RandomUtils.nextBytes(20);
    private String nodeIdStr=new String(nodeId, CharsetUtil.ISO_8859_1);
    private Map<Integer,PriorityQueue<Node>> tableMap=new HashMap<>();
    private Bucket bucket=new Bucket(0,null);


    public  List<Node> getSlef(){
        return Arrays.asList(new Node(nodeId,new InetSocketAddress("127.0.0.1",7000),0));
    }

    public List<Node> getForTop8(byte[] trargetBytes){
        int bucketIndex = getBucketIndex(trargetBytes);
        List<Node> l=new ArrayList<>();
        PriorityQueue<Node> pq = tableMap.get(bucketIndex);
        if(CollectionUtils.isEmpty(pq)){
            while(bucket.next != null){
                if(bucketIndex > bucket.getK()
                        && bucketIndex < bucket.next.getK()){

                    tableMap.get(bucket.next.getK()).stream().forEach(x->{
                        if(l.size()<8){
                            l.add(x);
                        }
                    });
                }
                bucket=bucket.next;
            }
            if(CollectionUtils.isEmpty(l)){
                tableMap.get(bucket.getK()).stream().forEach(x->{
                    if(l.size()<8){
                        l.add(x);
                    }
                });
            }

        }else{//如果不空 那么直接加 简单点来吧
            l.addAll(pq.stream().collect(Collectors.toList()));
        }
        return l;
    }

    public static void main(String[] args) {
        RoutingTable routingTable = new RoutingTable();
        Map<Integer, PriorityQueue<Node>> tableMap = routingTable.getTableMap();
        tableMap.put(0,new PriorityQueue<>());
        byte[] bytes = RandomUtils.nextBytes(26);
        Node node = new Node(bytes);
        node.setRank(1);
        for(int i=0;i<100;i++)
            routingTable.put(node);

    }
    public void put(Node node) {

        int bucketIndex = getBucketIndex(node);
        if(bucketIndex==0){//是自己就不用加入了
            return;
        }
        PriorityQueue<Node> pq = tableMap.get(bucketIndex);
        if(CollectionUtils.isEmpty(pq)){
            //如果是空 那么找最近的那个节点加入
            boolean isAdd=false;
            while(bucket.next != null){
                if(bucketIndex > bucket.getK()
                        && bucketIndex < bucket.next.getK()){
                     //先往小的里面放
                    node.setCurrentK(bucket.getK());
                    isAdd=putAccurate(tableMap.get(bucket.getK()),node,false,bucket,tableMap);
                    if(!isAdd){
                        node.setCurrentK(bucket.next.getK());
                        isAdd=putAccurate(tableMap.get(bucket.next.getK()),node,true,bucket,tableMap);
                    }
                }
                bucket=bucket.next;

            }
            if(!isAdd){
                //没有添加成功 那么往最后一个节点添加
                node.setCurrentK(bucket.getK());
                putAccurate(tableMap.get(bucket.getK()),node,true,bucket,tableMap);
            }

        }else{//如果不空 那么直接加 简单点来吧
            if(pq.size()<8){
                if(!pq.contains(node)){
                    node.setCurrentK(node.getK());
                    pq.add(node);
                }else{
                    reAdd(pq,node);
                }
            }else{
                pq.add(node);
                pq.poll();
            }
        }
    }

    public boolean reAdd(PriorityQueue<Node> pq,Node node){
        Node reAdd=new Node(RandomUtils.nextBytes(26));
        pq.stream().forEach(x->{
            if(x.getNodeId().equals(node.getNodeId())){
                x.setRank(x.getRank()+node.getRank());
                BeanUtils.copyProperties(x,reAdd);
            }
        });
        pq.remove(reAdd);
        pq.add(reAdd);
        return true;
    }

    @SneakyThrows
    public boolean putAccurate(PriorityQueue<Node> pq,Node node,boolean isSplit,Bucket bucket,Map<Integer,PriorityQueue<Node>> tableMap){
        boolean isAdd=false;
        if(pq.contains(node)){
            return reAdd(pq,node);
        }
        if(pq.size()<8){
            pq.add(node);
            isAdd=true;
        }
        if(isSplit && !isAdd){//需要分裂
            PriorityQueue<Node> priorityQueue=new PriorityQueue<Node>((x,y)->x.getRank()-y.getRank());
            priorityQueue.add(node);
            tableMap.putIfAbsent(node.getK(),priorityQueue);
            //创建新的k桶后需要吧两边的都放到自己的k桶里面 如果超过8个就丢了 最好是可以ping一下
            //先从小的开始放
            PriorityQueue<Node> collect1 = new PriorityQueue<>();
            collect1.addAll(tableMap.get(bucket.getK()).stream().filter(n -> {
                if (priorityQueue.size() < 8 && Math.abs(n.getK() - n.getCurrentK()) > Math.abs(n.getK() - node.getK())) {
                    n.setCurrentK(node.getK());
                    priorityQueue.add(n);
                    return false;
                }
                return true;
            }).collect(Collectors.toSet()));
            tableMap.put(bucket.getK(),CollectionUtils.isNotEmpty(collect1)?collect1:new PriorityQueue<Node>());
            if(bucket.next!=null && CollectionUtils.isNotEmpty(tableMap.get(bucket.next.getK()))){
                PriorityQueue<Node> collect = new PriorityQueue<>();
                collect.addAll(tableMap.get(bucket.next.getK()).stream().filter(n -> {
                    if (priorityQueue.size() < 8 && Math.abs(n.getK() - n.getCurrentK()) > Math.abs(n.getK() - node.getK())) {
                        n.setCurrentK(node.getK());
                        priorityQueue.add(n);
                        return false;
                    }
                    return true;
                }).collect(Collectors.toSet()));
                tableMap.put(bucket.next.getK(),CollectionUtils.isNotEmpty(collect)?collect:new PriorityQueue<Node>());
            }
            Bucket b=new Bucket(node.getK(),bucket.next);
            bucket.next=b;
            isAdd=true;
            node.setCurrentK(node.getK());
        }
        return isAdd;
    }

    public int getBucketIndex(Node node) {
        int index=160;
        byte[] bytes = getAllBit(node.getNodeIdBytes());
        byte[] nodeAllBit = getAllBit(nodeId);
        for(int i=0;i<bytes.length;i++){
            //高位一样距离越近
            if(bytes[i]==nodeAllBit[i]){
              index--;
            }else{
                break;
            }
        }
        node.setK(index);
        return index;
    }

    public int getBucketIndex(byte[] b) {
        int index=160;
        byte[] bytes = getAllBit(b);
        byte[] nodeAllBit = getAllBit(nodeId);
        for(int i=0;i<bytes.length;i++){
            //高位一样距离越近
            if(bytes[i]==nodeAllBit[i]){
                index--;
            }else{
                break;
            }
        }
        return index;
    }

    @Data
    @AllArgsConstructor
    public static class Bucket{

        private int k;

        private Bucket next;


    }
    /**
     * 转化为160位的bit
     * @param bytes
     * @return
     */
    public static byte[] getAllBit(byte[] bytes) {
        byte[] result = new byte[160];
        int index = 0;
        for (int i = 0; i < 20; i++) {
            byte b = bytes[i];
            //0-7 8-15
            int count=index+7;
            for (int j = 0; j < 8; j++) {
                result[count] = (byte) (b & 0x1);
                b = (byte) (b >> 1);
                count--;
            }
            index+=8;
        }
        return result;
    }

}
