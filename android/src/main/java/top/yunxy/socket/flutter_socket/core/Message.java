package top.yunxy.socket.flutter_socket.core;

import top.yunxy.socket.flutter_socket.jtt.MessageType;
import top.yunxy.socket.flutter_socket.util.HexUtil;

public class Message implements Comparable<Message> {
    private int msgId;
    private byte[] data;
    private int serialNo;
    private boolean divide;
    private int total;
    private int packageNo;
    private boolean skip = false;

    public Message(int msgId, byte[] data, int serialNo) {
        this.msgId = msgId;
        this.data = data;
        this.serialNo = serialNo;
        this.divide = false;
        this.total = 1;
        this.packageNo = 0;
    }

    public Message(int msgId, byte[] data, int serialNo, boolean skip) {
        this.msgId = msgId;
        this.data = data;
        this.serialNo = serialNo;
        this.divide = false;
        this.total = 1;
        this.packageNo = 0;
        this.skip = skip;
    }

    public Message(int msgId, byte[] data, int serialNo, boolean divide, int total, int packageNo) {
        this.msgId = msgId;
        this.data = data;
        this.serialNo = serialNo;
        this.divide = divide;
        this.total = total;
        this.packageNo = packageNo;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(int serialNo) {
        this.serialNo = serialNo;
    }

    public boolean isDivide() {
        return divide;
    }

    public void setDivide(boolean divide) {
        this.divide = divide;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPackageNo() {
        return packageNo;
    }

    public void setPackageNo(int packageNo) {
        this.packageNo = packageNo;
    }

    public boolean isSkip() {
        return skip;
    }

    @Override
    public String toString() {
        if (!this.divide) {
            return "Message{" +
                    "msgId=" + MessageType.get(msgId).toString() +
                    ", serialNo=" + serialNo +
                    ", skip=" + skip +
                    ", data=" + HexUtil.encode(data) +
                    '}';
        }
        return "Message{" +
                "msgId=" + MessageType.get(msgId).toString() +
                ", serialNo=" + serialNo +
                ", divide=" + true +
                ", total=" + total +
                ", packageNo=" + packageNo +
                ", skip=" + skip +
                ", data=" + HexUtil.encode(data) +
                '}';
    }

    @Override
    public int compareTo(Message message) {
        return this.packageNo - message.packageNo;
    }
}
