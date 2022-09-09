package top.yunxy.socket.flutter_socket.jtt;

/**
 * 实时音视频传输控制
 */
public class T9102 {
    //消息id
    private int msgId = 0x9101;

    ///逻辑通道号
    private int channelNo;

    ///控制指令: 0.关闭音视频传输指令 1.切换码流(增加暂停和继续) 2.暂停该通道所有流的发送 3.恢复暂停前流的发送,与暂停前的流类型一致 4.关闭双向对讲
    private int command;

    ///关闭音视频类型：0.关闭该通道有关的音视频数据 1.只关闭该通道有关的音频,保留该通道有关的视频 2.只关闭该通道有关的视频,保留该通道有关的音频
    private int closeType;

    ///码流类型：0.主码流 1.子码流
    private int streamType;

    public T9102() {
    }

    public T9102(byte[] bytes) {
        channelNo = bytes[0] & 0xff;
        command = bytes[1] & 0xff;
        closeType = bytes[2] & 0xff;
        streamType = bytes[3] & 0xff;
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

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public int getCloseType() {
        return closeType;
    }

    public void setCloseType(int closeType) {
        this.closeType = closeType;
    }

    public int getStreamType() {
        return streamType;
    }

    public void setStreamType(int streamType) {
        this.streamType = streamType;
    }

    @Override
    public String toString() {
        return "T9102{" +
                "channelNo=" + channelNo +
                ", command=" + command +
                ", closeType=" + closeType +
                ", streamType=" + streamType +
                '}';
    }
}
