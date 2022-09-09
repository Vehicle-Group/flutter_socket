package top.yunxy.socket.flutter_socket.jtt;

import java.util.ArrayList;
import java.util.List;

import top.yunxy.socket.flutter_socket.util.DataTypeUtil;

/**
 * 多媒体事件信息上传
 */
public class T0801 extends Base {
    //消息id
    private int msgId = 0x0801;

    ///多媒体数据id
    private int id;

    ///多媒体类型：0.图像 1.音频 2.视频
    private int type;

    ///多媒体格式编码：0.JPEG 1.TIF 2.MP3 3.WAV 4.WMV
    private int format;

    ///事件项编号: 0.平台下发命令 1.定时动作 2.抢劫报警触发 3.碰撞侧翻报警触发
    private int event;

    ///通道ID
    private int channelId;

    ///位置信息
    T0200 location;

    ///多媒体数据
    byte[] data;

    public T0801() {
    }

    public T0801(int id, int type, int format, int event, int channelId, T0200 location, byte[] data) {
        this.id = id;
        this.type = type;
        this.format = format;
        this.event = event;
        this.channelId = channelId;
        this.location = location;
        this.data = data;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public int getEvent() {
        return event;
    }

    public void setEvent(int event) {
        this.event = event;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public T0200 getLocation() {
        return location;
    }

    public void setLocation(T0200 location) {
        this.location = location;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public byte[] toBytes() {
        List<Byte> list = new ArrayList<>();
        for (byte b : DataTypeUtil.toDWORD(id)) {
            list.add(b);
        }
        list.add(DataTypeUtil.toBYTE(type));
        list.add(DataTypeUtil.toBYTE(format));
        list.add(DataTypeUtil.toBYTE(event));
        list.add(DataTypeUtil.toBYTE(channelId));
        for (byte b : location.toContent(true).getContent()) {
            list.add(b);
        }
        for (byte b : data) {
            list.add(b);
        }
        return DataTypeUtil.toBYTES(list);
    }

    @Override
    public MsgContent toContent() {
        return new MsgContent(msgId, toBytes());
    }

    @Override
    public String toString() {
        return "T0800{" +
                ", id=" + id +
                ", type=" + type +
                ", format=" + format +
                ", event=" + event +
                ", channelId=" + channelId +
                '}';
    }
}
