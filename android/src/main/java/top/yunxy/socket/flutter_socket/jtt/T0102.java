package top.yunxy.socket.flutter_socket.jtt;

import top.yunxy.socket.flutter_socket.util.DataTypeUtil;

/**
 * 鉴权
 */
public class T0102 extends Base {
    //消息id
    private int msgId = 0x0102;
    // 鉴权码
    private String code;

    public T0102() {
    }

    public T0102(String code) {
        this.code = code;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public byte[] toBytes() {
        byte[] data = DataTypeUtil.toSTRING(this.code);
        return data;
    }

    @Override
    public MsgContent toContent() {
        return new MsgContent(msgId, toBytes());
    }

    @Override
    public String toString() {
        return "T0102{" +
                "code='" + code + '\'' +
                '}';
    }
}
