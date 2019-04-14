package com.shentu.dht.handler;

import com.shentu.dht.bcodec.BTException;
import com.shentu.dht.bcodec.Bencode;
import com.shentu.dht.process.ProcessManager;
import com.shentu.dht.process.dto.MessageInfo;
import com.shentu.dht.process.dto.ProcessDto;
import com.shentu.dht.server.Sender;
import com.shentu.dht.util.DHTUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;


/**
 * Created by styb on 2019/3/12.
 */
@Slf4j
@AllArgsConstructor
public class TestHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final String LOG = "[DHT Handler]-";
    private Sender sender;

    private Bencode bencode;

    private ProcessManager processManager;

    private int num;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("TestHandlers start "+num);
        sender.add(num, ctx.channel());
        log.info("TestHandlers end num"+num +" id="+ctx.channel());
    }
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket pkg) throws Exception {
        byte[] bytes = getBytes(pkg);
        InetSocketAddress sender = pkg.sender();
        //解码为map
        Map<String, Object> map;
        try {
            map = bencode.decode(bytes, Map.class);
        } catch (BTException e) {
            e.printStackTrace();
            log.debug("{}消息解码异常.发送者:{}.异常:{}", LOG, sender, e.getMessage());
            return;
        } catch (Exception e) {
            e.printStackTrace();
            log.debug("{}消息解码异常.发送者:{}.异常:{}", LOG, sender, e.getMessage(), e);
            return;
        }
        //解析出MessageInfo
        MessageInfo messageInfo;
        try {
            messageInfo = DHTUtil.getMessageInfo(map);
        } catch (BTException e) {
            e.printStackTrace();
            log.debug("{}解析MessageInfo异常.异常:{}", LOG, e.getMessage());
            return;
        } catch (Exception e) {
            e.printStackTrace();
            log.debug("{}解析MessageInfo异常.异常:{}", LOG, e.getMessage(), e);
            return;
        }
        log.info("processManagerProcess num={}",num);
        processManager.process(new ProcessDto(messageInfo, map, sender,num));

    }

    private byte[] getBytes(DatagramPacket packet) {
        //读取消息到byte[]
        byte[] bytes = new byte[packet.content().readableBytes()];
        packet.content().readBytes(bytes);
        return bytes;
    }
}
