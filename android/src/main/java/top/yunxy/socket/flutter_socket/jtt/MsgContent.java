package top.yunxy.socket.flutter_socket.jtt;

public class MsgContent {
    private int msgId;
    private byte[] content;

    public MsgContent(int msgId, byte[] content) {
        this.msgId = msgId;
        this.content = content;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
