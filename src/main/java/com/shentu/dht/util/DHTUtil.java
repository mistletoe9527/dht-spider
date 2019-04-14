package com.shentu.dht.util;


import com.shentu.dht.bcodec.BTException;
import com.shentu.dht.enmu.CodeEnum;
import com.shentu.dht.enmu.MessageTypeEnmu;
import com.shentu.dht.enmu.MethodEnmu;
import com.shentu.dht.exception.RTException;
import com.shentu.dht.peer.Node;
import com.shentu.dht.process.dto.MessageInfo;
import io.netty.util.CharsetUtil;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by styb on 2019/3/4.
 */
public class DHTUtil {

    private static int maxMessageID = 1<<15;

    private static AtomicInteger msgIdGenerate=new AtomicInteger(1);

    public static String generateMessageId(){
        int value;
        if((value=msgIdGenerate.incrementAndGet())>maxMessageID){
            msgIdGenerate.set(1);
        }
        byte[] b=new byte[2];
        b[1]=(byte)(value & 0xff);
        b[0]=(byte)(value>>8 & 0xff);
        return new String(b, CharsetUtil.ISO_8859_1);
    }


    public static String generateNodeIdString() {
        return new String(RandomUtils.nextBytes(20), CharsetUtil.ISO_8859_1);
    }



    /**
     * Bean 转 Map
     */
    @SneakyThrows
    public static <T> Map<String, Object> beanToMap(T obj) {
        //实体类转map
        Map<String, Object> map = new LinkedHashMap<>();
        for(Class c=obj.getClass();c!=null;c=c.getSuperclass()){
            Field[] declaredFields = c.getDeclaredFields();
            if(!ArrayUtils.isEmpty(declaredFields)){
                for(Field f:declaredFields){
                    f.setAccessible(true);
                    Object val=f.get(obj);
                    map.put(f.getName(),
                            (val.getClass().isPrimitive()
                                    || val.getClass().getName().contains("java"))?
                                    val:beanToMap(val));
                }
            }
        }
        return map;
    }

    /**
     * 根据解析后的消息map,获取消息信息,例如 消息方法(ping/find_node等)/ 消息状态(请求/回复/异常)
     */
    public static MessageInfo getMessageInfo(Map<String, Object> map) throws Exception {
        MessageInfo messageInfo = new MessageInfo();

        /**
         * 状态 请求/回复/异常
         */
        String y = getParamString(map, "y", "y属性不存在.map:" + map);
        Optional<MessageTypeEnmu> yEnumOptional = getByCode(y, MessageTypeEnmu.class);
        messageInfo.setStatus(yEnumOptional.orElseThrow(()->new BTException("y属性值不正确.map:" + map)));

        /**
         * 消息id
         */
        String t = getParamString(map, "t", "t属性不存在.map:" + map);
        messageInfo.setMessageId(t);

        /**
         * 获取方法 ping/find_node等
         */
        //如果是请求, 直接从请求主体获取其方法
        if (messageInfo.getStatus().getKey().equals(MessageTypeEnmu.QUERY.getKey())) {
            String q = getParamString(map, "q", "q属性不存在.map:" + map);

            Optional<MethodEnmu> qEnumOptional = getByCode(q, MethodEnmu.class);
            messageInfo.setMethod(qEnumOptional.orElseThrow(()->new BTException("q属性值不正确.map:" + map)));

        } else  if (messageInfo.getStatus().getKey().equals(MessageTypeEnmu.RESPONSE.getKey()))  {
            Map<String, Object> rMap =getParamMap(map, "r", "r属性不存在.map:" + map);
            if(rMap.get("token") != null){
                messageInfo.setMethod(MethodEnmu.GET_PEERS);
            }else if(rMap.get("nodes") != null){
                messageInfo.setMethod(rMap.get("token") == null ? MethodEnmu.FIND_NODE : MethodEnmu.GET_PEERS);
            }else if(rMap.get("id")!=null){
                messageInfo.setMethod(MethodEnmu.PING);
            }else{
                throw new BTException("未知类型的回复消息.消息:" + map);
            }
        }
        return messageInfo;
    }

    public static   <T extends CodeEnum<X>,X> Optional<T> getByCode(X key,Class<T> enmuClass){

        for(T t:enmuClass.getEnumConstants()){
           if(t.getKey().equals(key)){
               return Optional.of(t);
           }
        }
        return Optional.empty();

    }

    /**
     * 从Map中获取String属性
     */
    public static String getParamString(Map<String, Object> map, String key, String log) {
        Object obj = getParam(map, key, log);
        return (String) obj;
    }




    /**
     * 从Map中获取Object属性
     */
    public static Object getParam(Map<String, Object> map, String key, String log) {
        Object obj = map.get(key);
        if (obj == null)
            throw new BTException(log);
        return obj;
    }


    /**
     * 从Map中获取Map属性
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getParamMap(Map<String, Object> map, String key, String log) {
        Object obj = getParam(map, key, log);
        return (Map<String, Object>) obj;
    }



    /**
     * 从回复的r对象中取出nodes
     */
    public static List<Node> getNodeListByRMap(Map<String, Object> rMap) {
        byte[] nodesBytes = getParamString(rMap, "nodes", "FIND_NODE,找不到nodes参数.rMap:" + rMap).getBytes(CharsetUtil.ISO_8859_1);
        List<Node> nodeList = new LinkedList<>();
        for (int i = 0; i + 26 < nodesBytes.length; i += 26) {
            //byte[26] 转 Node
            Node node = new Node(ArrayUtils.subarray(nodesBytes, i, i + 26));
            nodeList.add(node);
        }
        return nodeList;
    }

    public static String bytes2Ip(byte[] ipBytes){
        if(ArrayUtils.isEmpty(ipBytes) || ipBytes.length!=4){
            throw new RTException("转化ip失败！");
        }
        return String.join(".",
                String.valueOf(ipBytes[0] & 0xFF),
                String.valueOf(ipBytes[1] & 0xFF),
                String.valueOf(ipBytes[2] & 0xFF),
                String.valueOf(ipBytes[3] & 0xFF));
    }

    public static int bytes2Port(byte[] portBytes){
        if(ArrayUtils.isEmpty(portBytes) || portBytes.length!=2){
            throw new RTException("转化port失败！");
        }
        return (portBytes[1] & 0xff) | (portBytes[0] & 0xff) << 8;
    }

    public static byte[] int2TwoBytes(int value) {
        byte[] des = new byte[2];
        des[1] = (byte) (value & 0xff);
        des[0] = (byte) ((value >> 8) & 0xff);
        return des;
    }

    public static List<String> getParamList(Map<String, Object> map, String key, String log) {
        Object obj = getParam(map, key, log);
        return (List<String>) obj;
    }

    @SneakyThrows
    public static byte[] hexStr2Bytes(String hexStr) {
        try{
            return Hex.decodeHex(hexStr.toCharArray());
        }catch (Exception e){

        }
        return null;

    }

    /**
     * byte[] 转 16进制字符串
     */
    public static String bytes2HexStr(byte[] bytes) {
        return Hex.encodeHexString(bytes);
    }

    /**
     * 包装generateSimilarInfoHash()方法参数和返回值为string
     */
    public static String generateSimilarInfoHashString(byte[] nodeId,int num) {
        return new String(generateSimilarNodeId(nodeId,num),CharsetUtil.ISO_8859_1);
    }

    /**
     * 生成一个和指定info_hash(nodeIds)异或值仅相差若干位的info_hash(nodeIds)
     */
    public static byte[] generateSimilarNodeId(byte[] hash, int num) {
        byte[] result = new byte[hash.length];
        //拷贝前(length-num)位到新数组
        System.arraycopy(hash,0,result,0,hash.length - num);
        //拷贝随机数组到后num位
        System.arraycopy(RandomUtils.nextBytes(num),0,result,hash.length - num,num);
        return result;
    }

    /**
     * 从udp返回的sender属性中,提取出ip
     */
    public static String getIpBySender(InetSocketAddress sender) {
        return sender.getAddress().toString().substring(1);
    }


    /**
     * int 转 byte[4]
     */
    public static byte[] int2Bytes(int value) {
        byte[] des = new byte[4];
        des[3] = (byte) (value & 0xff);
        des[2] = (byte) ((value >> 8) & 0xff);
        des[1] = (byte) ((value >> 16) & 0xff);
        des[0] = (byte) ((value >> 24) & 0xff);
        return des;
    }


}
