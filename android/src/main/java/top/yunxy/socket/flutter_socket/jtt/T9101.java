package top.yunxy.socket.flutter_socket.jtt;

import top.yunxy.socket.flutter_socket.util.DataTypeUtil;

/**
 * 实时音视频传输请求
 */
public class T9101 {
    //消息id
    private int msgId = 0x9101;

    ///ip地址长度
    private int ipLen;

    ///ip地址
    private String ip;

    ///tcp监听端口
    private int tcpPort;

    ///udp监听端口
    private int udpPort;

    ///逻辑通道号
    private int channelNo;

    ///数据类型：0.音视频 1.视频 2.双向对讲 3.监听 4.中心广播 5.透传
    private int mediaType;

    ///码流类型：0.主码流 1.子码流
    private int streamType;

    public T9101() {
    }

    public T9101(byte[] bytes) {
        ipLen = bytes[0] & 0xff;
        ip = new String(DataTypeUtil.toBYTES(bytes, 1, ipLen + 1));
        tcpPort = (bytes[ipLen + 1] & 0xff) << 8 | (bytes[ipLen + 2] & 0xff);
        udpPort = (bytes[ipLen + 3] & 0xff) << 8 | (bytes[ipLen + 4] & 0xff);
        channelNo = bytes[ipLen + 5] & 0xff;
        mediaType = bytes[ipLen + 6] & 0xff;
        streamType = bytes[ipLen + 7] & 0xff;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public int getIpLen() {
        return ipLen;
    }

    public void setIpLen(int ipLen) {
        this.ipLen = ipLen;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }

    public int getChannelNo() {
        return channelNo;
    }

    public void setChannelNo(int channelNo) {
        this.channelNo = channelNo;
    }

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public int getStreamType() {
        return streamType;
    }

    public void setStreamType(int streamType) {
        this.streamType = streamType;
    }

    @Override
    public String toString() {
        return "T9101{" +
                "ipLen=" + ipLen +
                ", ip='" + ip + '\'' +
                ", tcpPort=" + tcpPort +
                ", udpPort=" + udpPort +
                ", channelNo=" + channelNo +
                ", mediaType=" + mediaType +
                ", streamType=" + streamType +
                '}';
    }
}
