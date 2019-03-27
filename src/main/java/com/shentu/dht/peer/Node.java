package com.shentu.dht.peer;

import com.shentu.dht.bcodec.BTException;
import com.shentu.dht.util.DHTUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by styb on 2019/3/13.
 */
@AllArgsConstructor
@Getter
@Setter
public class Node implements Comparable<Node>{

    private String nodeId;//16进制字符串

    private String ip;

    private Integer port;

    private Date updateTime;//最后更新时间

    private byte[] nodeIdBytes;//20字节

    private Integer k=0;//k桶应该有的位置

    private Integer currentK=0;//当前的位置

    private Integer rank=0;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        return Arrays.equals(nodeIdBytes, node.nodeIdBytes);

    }

    @Override
    public int hashCode() {
        return nodeIdBytes != null ? Arrays.hashCode(nodeIdBytes) : 0;
    }

    @Override
    public int compareTo(Node o) {
        return this.getRank() - o.getRank();
    }

    /**
     * byte[26] 转 Node
     */
    public Node(byte[] bytes) {
        if (bytes.length != 26)
            throw new BTException("转换为Node需要bytes长度为26,当前为:" + bytes.length);
        //nodeIds
        nodeIdBytes = ArrayUtils.subarray(bytes, 0, 20);

        //ip
        ip = DHTUtil.bytes2Ip(ArrayUtils.subarray(bytes, 20, 24));

        //ports
        port = DHTUtil.bytes2Port( ArrayUtils.subarray(bytes, 24, 26));

        nodeId= Hex.encodeHexString(nodeIdBytes);

    }

    public Node(byte[] nodeIdBytes, InetSocketAddress sender, Integer rank) {
        this.nodeIdBytes = nodeIdBytes;
        this.ip = getIpBySender(sender);
        this.port = sender.getPort();
        this.rank = rank;
        nodeId= Hex.encodeHexString(nodeIdBytes);
    }

    /**
     * List<Node> 转 byte[]
     */
    public static byte[] toBytes(List<Node> nodes) {
        if(CollectionUtils.isEmpty(nodes))
            return new byte[0];
        byte[] result = new byte[nodes.size() * 26];
        for (int i = 0; i + 26 <= result.length; i+=26) {
            System.arraycopy(nodes.get(i/26).toBytes(),0,result,i,26);
        }
        return result;
    }

    /**
     * Node 转 byte[]
     */
    public byte[] toBytes() {
        check();
        //nodeIds
        byte[] nodeBytes = new byte[26];
        System.arraycopy(nodeIdBytes, 0, nodeBytes, 0, 20);

        //ip
        String[] ips = StringUtils.split(ip, ".");
        if(ips.length != 4)
            throw new BTException("该节点IP有误,节点信息:" + this);
        byte[] ipBytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            ipBytes[i] = (byte) Integer.parseInt(ips[i]);
        }
        System.arraycopy(ipBytes, 0, nodeBytes, 20, 4);

        //ports
        byte[] portBytes = DHTUtil.int2TwoBytes(port);
        System.arraycopy(portBytes, 0, nodeBytes, 24, 2);

        return nodeBytes;
    }

    /**
     * 检查该节点信息是否完整
     */
    public void check() {
        //此处对小于1024的私有端口.不作为错误.
        if(nodeIdBytes == null || nodeIdBytes.length != 20 ||
                StringUtils.isBlank(ip) || port == null ||  port > 65535)
            throw new BTException("该节点信息有误:" + this);
    }
    /**
     * 从udp返回的sender属性中,提取出ip
     */
    public static String getIpBySender(InetSocketAddress sender) {
        return sender.getAddress().toString().substring(1);
    }
    /**
     * Node 转 InetSocketAddress
     */
    public InetSocketAddress toAddress() {
        return new InetSocketAddress(this.ip, this.port);
    }

}
