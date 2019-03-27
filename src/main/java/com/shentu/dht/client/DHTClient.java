package com.shentu.dht.client;

import com.shentu.dht.bcodec.BTException;
import com.shentu.dht.peer.Node;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by styb on 2019/3/4.
 */
public class DHTClient {


    /** udp发送器 */
    private DatagramSocket sender = null;

    /** ip地址 */
    InetAddress inetAddress = null;
    int port = -1;
    private DatagramPacket sendPacket;

    public DHTClient(String ipAdress, int port) {
        try {
            sender = new DatagramSocket();
            sender.setSoTimeout(3000);
            this.inetAddress = InetAddress.getByName(ipAdress);
            this.port = port;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public String sendData(String data) {
        if (checkFieldStatus()) {
            sendPacket = new DatagramPacket(data.getBytes(), data.getBytes().length, inetAddress, port);
            try {
                sender.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] bytes = new byte[10240];
            DatagramPacket recvPacket = new DatagramPacket(bytes, bytes.length);
            try {
                sender.receive(recvPacket);
                String resultMsg = new String(recvPacket.getData(), 0, recvPacket.getLength(), CharsetUtil.ISO_8859_1);
                return resultMsg;
            }
            catch (SocketTimeoutException e)
            {
//                e.printStackTrace();
//                System.out.println(inetAddress.toString() + ":" + port + " connected time out.");
            }
            catch (IOException e) {
//                e.printStackTrace();
            }
        }
        return null;
    }

    public String findNodeOnDHT(byte[] nodeId,byte[] tragetId) {
        String data = "d1:ad2:id20:"+nodeId+"6:target20:"+tragetId+"e1:q9:find_node1:t2:aa1:y1:qe";
        return sendData(data);
    }

    public String getPeerNodeOnDHTgetPeerNodeOnDHT(byte[] nodeId,byte[] infoHash) {

        String data = "d1:ad2:id20:"+nodeId+"9:info_hash20:"+"0792FB8848929BA9831A8D10495E7A41DBBC0AB0"+"e1:q9:get_peers1:t2:aa1:y1:qe";
        return sendData(data);
    }
    public String getPeerNodeOnDHT2(String nodeId,String infoHash) {
        String data = "d1:ad2:id20:"+nodeId+"9:info_hash20:"+infoHash+"e1:q9:get_peers1:t2:aa1:y1:qe";
        return sendData(data);
    }



    /**
     * 检查客户端状态
     * @return
     */
    private boolean checkFieldStatus()
    {
        if (sender != null && inetAddress != null && port != -1) {
            return true;
        }
        return false;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public DHTClient setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
        return this;
    }

    public int getPort() {
        return port;
    }

    public DHTClient setPort(int port) {
        this.port = port;
        return this;
    }

    public DatagramSocket getSender() {
        return sender;
    }

    public DHTClient setSender(DatagramSocket sender) {
        this.sender = sender;
        return this;
    }

    public DatagramPacket getSendPacket() {
        return sendPacket;
    }

    public DHTClient setSendPacket(DatagramPacket sendPacket) {
        this.sendPacket = sendPacket;
        return this;
    }



}
