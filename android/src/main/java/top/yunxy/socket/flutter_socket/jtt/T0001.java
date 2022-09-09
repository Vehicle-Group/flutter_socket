package top.yunxy.socket.flutter_socket.jtt;

import java.util.ArrayList;
import java.util.List;

import top.yunxy.socket.flutter_socket.util.DataTypeUtil;

/**
 * 终端通用应答
 */
public class T0001 extends Base {
    //消息id
    private int msgId = 0x0001;

    ///应答流水号
    private int answerSerialNo;

    ///应答Id
    private int answerId;

    ///结果( 0：成功/确认；1：失败；2：消息有误；3：不支持；4：报警处理确认 )
    private int result;

    public T0001() {
    }

    public T0001(int answerSerialNo, int answerId, int result) {
        this.answerSerialNo = answerSerialNo;
        this.answerId = answerId;
        this.result = result;
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

    public int getAnswerId() {
        return answerId;
    }

    public void setAnswerId(int answerId) {
        this.answerId = answerId;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    @Override
    public byte[] toBytes() {
        List<Byte> list = new ArrayList<>();
        //应答流水号
        for (byte b : DataTypeUtil.toWORD(answerSerialNo)) {
            list.add(b);
        }
        //应答Id
        for (byte b : DataTypeUtil.toWORD(answerId)) {
            list.add(b);
        }
        //结果
        list.add(DataTypeUtil.toBYTE(result));
        return DataTypeUtil.toBYTES(list);
    }

    @Override
    public MsgContent toContent() {
        return new MsgContent(msgId, toBytes());
    }
}
