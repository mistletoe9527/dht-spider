package com.shentu.dht.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.shentu.dht.bcodec.Bencode;
import com.shentu.dht.client.ESClient;
import com.shentu.dht.peer.RoutingTable;
import com.shentu.dht.process.dto.MetaData;
import com.shentu.dht.util.DHTUtil;
import com.shentu.dht.util.FileUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by styb on 2019/4/2.
 */
@Slf4j
public class SearchMetaDataHandler extends SimpleChannelInboundHandler<ByteBuf> {

    public static final byte[] HANDSHAKE_BYTES = {19, 66, 105, 116, 84, 111, 114, 114, 101, 110, 116, 32, 112, 114,
            111, 116, 111, 99, 111, 108, 0, 0, 0, 0, 0, 16, 0, 1};
    private Bencode bencode;
    private String infoHash;
    private RoutingTable routingTable;
    private ESClient esClient;
    private int bs=0;
    @Getter
    @Setter
    private byte[] result;
    public SearchMetaDataHandler(Bencode bencode,RoutingTable routingTable,ESClient esClient,String infoHash){
        this.bencode=bencode;
        this.routingTable=routingTable;
        this.infoHash=infoHash;
        this.esClient=esClient;
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        byte[] infoHash = DHTUtil.hexStr2Bytes(this.infoHash);
        byte[] sendBytes = new byte[68];
        System.arraycopy(HANDSHAKE_BYTES, 0, sendBytes, 0, 28);
        System.arraycopy(infoHash, 0, sendBytes, 28, 20);
        System.arraycopy(routingTable.getNodeId(), 0, sendBytes, 48, 20);
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer(sendBytes));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        String messageStr = new String(bytes, CharsetUtil.ISO_8859_1);
        if(bytes[0]==19){
            //handshake
            sendHandshakeMsg(ctx);
        }

        if (messageStr.contains("ut_metadata") && messageStr.contains("metadata_size")) {
            log.info("support009hash="+this.infoHash);
            sendMetadataRequest(ctx, messageStr);
        }

        //如果是分片信息
        if (messageStr.contains("msg_type")) {
            String resultStr = messageStr.substring(messageStr.indexOf("ee") + 2, messageStr.length());
            byte[] resultStrBytes = resultStr.getBytes(CharsetUtil.ISO_8859_1);
            if (ArrayUtils.isNotEmpty(result)) {
                result=ArrayUtils.addAll(result, resultStrBytes);
            } else {
                result=resultStrBytes;
            }

            String metadataStr = new String(result, CharsetUtil.UTF_8);
            String metadataBencodeStr="";
            try{
                metadataBencodeStr= metadataStr.substring(0, metadataStr.indexOf("6:pieces")) + "e";
            }catch (Exception e){
                return;
            }
            log.info("dolownSuccess bs={}",bs);
            Bencode bencode = new Bencode(CharsetUtil.UTF_8);//注意,此处都优先使用utf-8编码
            Map<String, Object> resultMap = bencode.decode(metadataBencodeStr.getBytes(CharsetUtil.UTF_8), Map.class);
            List<String> nameInfo=new ArrayList<>();
            if(resultMap.containsKey("files")){
                List<MetaData> list = ((JSONArray) JSONArray.parse(JSONObject.toJSONString(resultMap.get("files"))))
                        .stream().map(v -> {
                            String name = ((JSONObject) v).getString("path");
                            Long length = ((JSONObject) v).getLong("length");
                            return new MetaData(infoHash, length, name,"");
                        }).collect(Collectors.toList());
                String name="";
                Long legth=0l;
                for(MetaData m:list){
                    name+=m.getName().replace("[","").replace("]","").replace("\\","").replace("\"","")+" ";
                    legth+=m.getLength();
                    nameInfo.add(m.getName().replace("[","").replace("]","").replace("\\","").replace("\"", ""));
                }
                writeData(ctx,name);
                esClient.index(new MetaData(this.infoHash,legth,name,JSONObject.toJSONString(nameInfo)));
            }else{
                writeData(ctx, (String) resultMap.get("name"));
                nameInfo.add((String)resultMap.get("name"));
                esClient.index(new MetaData(this.infoHash,Long.parseLong(String.valueOf(resultMap.get("length"))), String.valueOf(resultMap.get("name")), JSONObject.toJSONString(nameInfo)));
            }
        }



    }

    public void writeData(ChannelHandlerContext ctx,String name){
        try{
            InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            FileUtil.wirteData(this.infoHash + ":" + inetSocketAddress.getAddress() + ":" + inetSocketAddress.getPort()+":"+name + "\r\n");
        }catch (Exception e){}
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        log.error("error in SearchMetaDataHandler msg={}",cause.getMessage());
        ctx.close();
    }

    public void sendHandshakeMsg(ChannelHandlerContext ctx) throws Exception{
        Map<String, Object> extendMessageMap = new LinkedHashMap<>();
        Map<String, Object> extendMessageMMap = new LinkedHashMap<>();
        extendMessageMMap.put("ut_metadata", 1);
        extendMessageMap.put("m", extendMessageMMap);
        byte[] tempExtendBytes = bencode.encode(extendMessageMap);
        byte[] extendMessageBytes = new byte[tempExtendBytes.length + 6];
        extendMessageBytes[4] = 20;
        extendMessageBytes[5] = 0;
        byte[] lenBytes = DHTUtil.int2Bytes(tempExtendBytes.length + 2);
        System.arraycopy(lenBytes, 0, extendMessageBytes, 0, 4);
        System.arraycopy(tempExtendBytes, 0, extendMessageBytes, 6, tempExtendBytes.length);
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer(extendMessageBytes));

    }

    @SneakyThrows
    private void sendMetadataRequest(ChannelHandlerContext ctx, String s){
        int ut_metadata= Integer.parseInt(s.substring(s.indexOf("ut_metadatai") + 12, s.indexOf("ut_metadatai") + 13));
        String str=s.substring(s.indexOf("metadata_sizei") + 14, s.length());
        int metadata_size=Integer.parseInt(str.substring(0, str.indexOf("e")));
        //分块数
        int blockSize = (int) Math.ceil((double) metadata_size / (16 << 10));
        bs=blockSize;
        log.info("blocksize="+blockSize);
        //发送metadata请求
        for (int i = 0; i < blockSize; i++) {
            Map<String, Object> metadataRequestMap = new LinkedHashMap<>();
            metadataRequestMap.put("msg_type", 0);
            metadataRequestMap.put("piece", i);
            byte[] metadataRequestMapBytes = bencode.encode(metadataRequestMap);
            byte[] metadataRequestBytes = new byte[metadataRequestMapBytes.length + 6];
            metadataRequestBytes[4] = 20;
            metadataRequestBytes[5] = (byte) ut_metadata;
            byte[] lenBytes = DHTUtil.int2Bytes(metadataRequestMapBytes.length + 2);
            System.arraycopy(lenBytes, 0, metadataRequestBytes, 0, 4);
            System.arraycopy(metadataRequestMapBytes, 0, metadataRequestBytes, 6, metadataRequestMapBytes.length);
            ctx.channel().writeAndFlush(Unpooled.copiedBuffer(metadataRequestBytes));
        }
    }


}
