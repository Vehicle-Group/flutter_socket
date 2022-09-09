package top.yunxy.socket.flutter_socket.jtt;

import java.util.ArrayList;
import java.util.List;

/// 补传分包请求
public class T8003 {
    //消息id
    private int msgId = 0x8003;

    ///流水号
    private int serialNo;

    ///重传包总数
    private int total;

    /// 重传包id列表
    private List<Integer> ids;

    public T8003() {
    }

    public T8003(byte[] bytes) {
        serialNo = (bytes[0] & 0xff) << 24 |
                (bytes[1] & 0xff) << 16 |
                (bytes[2] & 0xff) << 8 |
                (bytes[3] & 0xff);
        if (bytes.length > 4) {
            total = bytes[4] & 0xff;
            final int start = 5;
            List<Integer> ids = new ArrayList<>();
            for (int i = 0; i < 2 * total; i += 2) {
                final int e = (bytes[start + i] & 0xff) << 8 | (bytes[start + i + 1] & 0xff);
                ids.add(e);
            }
        }
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public int getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(int serialNo) {
        this.serialNo = serialNo;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<Integer> getIds() {
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }

    @Override
    public String toString() {
        return "T8003{" +
                "serialNo=" + serialNo +
                ", total=" + total +
                ", ids=" + ids +
                '}';
    }
}
