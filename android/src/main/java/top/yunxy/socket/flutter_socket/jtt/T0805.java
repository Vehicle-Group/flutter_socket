package top.yunxy.socket.flutter_socket.jtt;

import java.util.ArrayList;
import java.util.List;

import top.yunxy.socket.flutter_socket.util.DataTypeUtil;

/**
 * 多媒体事件信息上传
 */
public class T0805 extends Base {
    //消息id
    private int msgId = 0x0805;

    ///应答流水号
    private int answerSerialNo;

    ///结果( 0：成功/确认；1：失败；2：通道不支持 )
    private int result;

    ///多媒体ID个数：拍摄成功的多媒体个数
    private int num;

    ///多媒体id列表
    List<Integer> ids;

    public T0805() {
    }

    public T0805(int answerSerialNo, int result, int num, List<Integer> ids) {
        this.answerSerialNo = answerSerialNo;
        this.result = result;
        this.num = num;
        this.ids = ids;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public int getAnswerSerialNo() {
        return answerSerialNo;
    }

    public void setAnswerSerialNo(int answerSerialNo) {
        this.answerSerialNo = answerSerialNo;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public List<Integer> getIds() {
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }

    @Override
    public byte[] toBytes() {
        List<Byte> list = new ArrayList<>();
        for (byte b : DataTypeUtil.toWORD(answerSerialNo)) {
            list.add(b);
        }
        list.add(DataTypeUtil.toBYTE(result));
        for (byte b : DataTypeUtil.toWORD(num)) {
            list.add(b);
        }
        for (Integer id : ids) {
            for (byte b : DataTypeUtil.toDWORD(id)) {
                list.add(b);
            }
        }
        return DataTypeUtil.toBYTES(list);
    }

    @Override
    public MsgContent toContent() {
        return new MsgContent(msgId, toBytes());
    }

    @Override
    public String toString() {
        return "T0805{" +
                ", answerSerialNo=" + answerSerialNo +
                ", result=" + result +
                ", num=" + num +
                ", ids=" + ids +
                '}';
    }
}
