package top.yunxy.socket.flutter_socket.jtt;

/**
 * 心跳
 */
public class T0002 extends Base {
    private int msgId = 0x0002;

    public T0002() {
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    @Override
    public byte[] toBytes() {
        byte[] msgContent = {};
        return msgContent;
    }

    @Override
    public MsgContent toContent() {
        return new MsgContent(msgId, toBytes());
    }
}
