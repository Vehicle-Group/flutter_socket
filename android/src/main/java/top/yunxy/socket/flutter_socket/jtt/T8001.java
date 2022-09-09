package top.yunxy.socket.flutter_socket.jtt;

/**
 * 鉴权
 */
public class T8001 {
    //消息id
    private int msgId = 0x8001;
    ///应答流水号
    private int answerSerialNo;

    ///应答Id
    private int answerId;

    ///结果( 0：成功/确认；1：失败；2：消息有误；3：不支持；4：报警处理确认 )
    private int result;

    public T8001() {
    }

    public T8001(byte[] bytes) {
        answerSerialNo = (bytes[0] & 0xff) << 8 | (bytes[1] & 0xff);
        answerId = (bytes[2] & 0xff) << 8 | (bytes[3] & 0xff);
        result = bytes[4] & 0xff;
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
    public String toString() {
        return "T8001{" +
                "answerSerialNo=" + answerSerialNo +
                ", answerId=" + MessageType.get(answerId).toString() +
                ", result=" + result +
                '}';
    }
}
