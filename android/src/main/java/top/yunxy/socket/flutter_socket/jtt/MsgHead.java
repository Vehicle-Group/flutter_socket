package top.yunxy.socket.flutter_socket.jtt;

import java.util.ArrayList;
import java.util.List;

import top.yunxy.socket.flutter_socket.util.DataTypeUtil;

public class MsgHead extends Base {
    //消息Id
    private int msgId;
    //是否分包
    private boolean divide;
    //是否rsa加密
    private boolean rsa;
    //消息长度
    private int msgLen;
    //终端手机号
    private String phone;
    //流水号
    private int serialNo;
    //消息总数
    private int msgTotal;
    //包序号
    private int packageNum;

    public MsgHead(byte[] bytes) {
        this.toAnalysis(bytes);
    }

    public MsgHead(int msgId, int msgLen, String phone) {
        this.msgId = msgId;
        this.divide = false;
        this.rsa = false;
        this.msgLen = msgLen;
        this.phone = phone;
        this.serialNo = 0;
        this.msgTotal = 0;
        this.packageNum = 0;
    }

    public MsgHead(int msgId, int msgLen, String phone, int serialNo) {
        this.msgId = msgId;
        this.divide = false;
        this.rsa = false;
        this.msgLen = msgLen;
        this.phone = phone;
        this.serialNo = serialNo;
        this.msgTotal = 0;
        this.packageNum = 0;
    }

    public MsgHead(int msgId, boolean divide, int msgLen, String phone, int serialNo, int msgTotal, int packageNum) {
        this.msgId = msgId;
        this.divide = divide;
        this.rsa = false;
        this.msgLen = msgLen;
        this.phone = phone;
        this.serialNo = serialNo;
        this.msgTotal = msgTotal;
        this.packageNum = packageNum;
    }

    public MsgHead(int msgId, boolean divide, boolean rsa, int msgLen, String phone, int serialNo, int msgTotal, int packageNum) {
        this.msgId = msgId;
        this.divide = divide;
        this.rsa = rsa;
        this.msgLen = msgLen;
        this.phone = phone;
        this.serialNo = serialNo;
        this.msgTotal = msgTotal;
        this.packageNum = packageNum;
    }

    public int getMsgId() {
        return msgId;
    }

    public boolean isDivide() {
        return divide;
    }

    public boolean isRsa() {
        return rsa;
    }

    public int getMsgLen() {
        return msgLen;
    }

    public String getPhone() {
        return phone;
    }

    public int getSerialNo() {
        return serialNo;
    }

    public int getMsgTotal() {
        return msgTotal;
    }

    public int getPackageNum() {
        return packageNum;
    }

    @Override
    public byte[] toBytes() {
        ArrayList<Byte> list = new ArrayList<>();
        //消息id
        for (byte b : DataTypeUtil.toWORD(this.msgId)) {
            list.add(b);
        }
        //消息体属性
        for (byte b : toMsgAttr()) {
            list.add(b);
        }
        //终端手机号
        for (byte b : DataTypeUtil.toBCD(this.phone)) {
            list.add(b);
        }
        //消息流水号
        for (byte b : DataTypeUtil.toWORD(this.serialNo)) {
            list.add(b);
        }
        //消息包封装项
        if (this.divide) {
            //消息总包数
            for (byte b : DataTypeUtil.toWORD(this.msgTotal)) {
                list.add(b);
            }
            //包序号
            for (byte b : DataTypeUtil.toWORD(this.packageNum)) {
                list.add(b);
            }
        }
        return DataTypeUtil.toBYTES(list);
    }

    @Override
    public MsgContent toContent() {
        return new MsgContent(0, toBytes());
    }

    @Override
    public String toString() {
        return "MsgHead{" +
                "msgId=" + msgId +
                ", divide=" + divide +
                ", rsa=" + rsa +
                ", msgLen=" + msgLen +
                ", phone='" + phone + '\'' +
                ", serialNo=" + serialNo +
                ", msgTotal=" + msgTotal +
                ", packageNum=" + packageNum +
                '}';
    }

    private byte[] toMsgAttr() {
        StringBuilder s = new StringBuilder("00");
        if (this.divide) {
            s.append("1");
        } else {
            s.append("0");
        }
        if (this.rsa) {
            s.append("001");
        } else {
            s.append("000");
        }
        String str = Integer.toBinaryString(this.msgLen);
        if (str.length() >= 10) {
            str = str.substring(str.length() - 10, str.length());
        } else {
            for (int i = 0; i < (10 - str.length()); i++) {
                s.append("0");
            }
        }
        s.append(str);
        return DataTypeUtil.toBYTES(s.toString());
    }

    private void toAnalysis(byte[] bytes) {
        //消息Id
        List<Byte> list = new ArrayList<>();
        list.add(bytes[0]);
        list.add(bytes[1]);
        this.msgId = DataTypeUtil.bytesToInt(list);
        //消息体属性
        list = new ArrayList<>();
        list.add(bytes[2]);
        list.add(bytes[3]);
        String binaryStr = Integer.toBinaryString(DataTypeUtil.bytesToInt(list));
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < (16 - binaryStr.length()); i++) {
            s.append("0");
        }
        s.append(binaryStr);
        binaryStr = s.toString();
        if ("1".equals(binaryStr.substring(2, 3))) {
            this.divide = true;
        } else {
            this.divide = false;
        }
        if ("1".equals(binaryStr.substring(5, 6))) {
            this.rsa = true;
        } else {
            this.rsa = false;
        }
        this.msgLen = Integer.valueOf("00" + binaryStr.substring(6), 2);
        //终端手机号
        list = new ArrayList<>();
        list.add(bytes[4]);
        list.add(bytes[5]);
        list.add(bytes[6]);
        list.add(bytes[7]);
        list.add(bytes[8]);
        list.add(bytes[9]);
        this.phone = DataTypeUtil.toReverBCD(list);
        //消息流水号
        list = new ArrayList<>();
        list.add(bytes[10]);
        list.add(bytes[11]);
        this.serialNo = DataTypeUtil.bytesToInt(list);
        //消息包封装项
        if (this.divide) {
            //消息总包数
            list = new ArrayList<>();
            list.add(bytes[12]);
            list.add(bytes[13]);
            this.msgTotal = DataTypeUtil.bytesToInt(list);
            //包序号
            list = new ArrayList<>();
            list.add(bytes[14]);
            list.add(bytes[15]);
            this.packageNum = DataTypeUtil.bytesToInt(list);
        }
    }
}
