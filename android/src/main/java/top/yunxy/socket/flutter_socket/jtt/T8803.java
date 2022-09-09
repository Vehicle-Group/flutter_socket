package top.yunxy.socket.flutter_socket.jtt;

import top.yunxy.socket.flutter_socket.util.DataTypeUtil;

/**
 * 多媒体事件信息上传
 */
public class T8803 {
    //消息id
    private int msgId = 0x8803;

    ///多媒体类型：0.图像 1.音频 2.视频
    int type;

    ///通道ID
    int channelId;

    ///事件项编号
    int event;

    ///开始时间
    String startTime;

    ///结束时间
    String endTime;

    ///删除标志
    int delete;

    public T8803() {
    }

    public T8803(byte[] bytes) {
        type = bytes[0] & 0xff;
        channelId = bytes[1] & 0xff;
        event = bytes[2] & 0xff;
        startTime = DataTypeUtil.toReverBCD(DataTypeUtil.toBYTES(bytes, 3, 9));
        endTime = DataTypeUtil.toReverBCD(DataTypeUtil.toBYTES(bytes, 9, 15));
        delete = bytes[15] & 0xff;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public int getEvent() {
        return event;
    }

    public void setEvent(int event) {
        this.event = event;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getDelete() {
        return delete;
    }

    public void setDelete(int delete) {
        this.delete = delete;
    }

    @Override
    public String toString() {
        return "T8803{" +
                "type=" + type +
                ", channelId=" + channelId +
                ", event=" + event +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", delete=" + delete +
                '}';
    }
}
