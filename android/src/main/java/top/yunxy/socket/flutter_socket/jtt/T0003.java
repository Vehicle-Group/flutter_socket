package top.yunxy.socket.flutter_socket.jtt;

/**
 * 终端注销
 */
public class T0003 extends Base {
    //消息id
    private int msgId = 0x0003;

    public T0003() {
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    @Override
    public byte[] toBytes() {
        return new byte[]{};
    }

    @Override
    public MsgContent toContent() {
        return new MsgContent(msgId, toBytes());
    }

    @Override
    public String toString() {
        return "T0003{" +
                '}';
    }
}
