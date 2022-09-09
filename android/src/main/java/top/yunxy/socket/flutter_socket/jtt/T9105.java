package top.yunxy.socket.flutter_socket.jtt;

/**
 * 实时音视频传输状态通知
 */
public class T9105 {
    //消息id
    private int msgId = 0x9105;

    ///逻辑通道号
    private int channelNo;

    ///丢包率
    private int packetLossRate;

    public T9105() {
    }

    public T9105(byte[] bytes) {
        channelNo = bytes[0] & 0xff;
        packetLossRate = bytes[1] & 0xff;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public int getChannelNo() {
        return channelNo;
    }

    public void setChannelNo(int channelNo) {
        this.channelNo = channelNo;
    }

    public int getPacketLossRate() {
        return packetLossRate;
    }

    public void setPacketLossRate(int packetLossRate) {
        this.packetLossRate = packetLossRate;
    }

    @Override
    public String toString() {
        return "T9105{" +
                "channelNo=" + channelNo +
                ", packetLossRate=" + packetLossRate +
                '}';
    }
}
