package top.yunxy.socket.flutter_socket.util;

import top.yunxy.socket.flutter_socket.jtt.MsgContent;
import top.yunxy.socket.flutter_socket.jtt.MsgHead;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataTypeUtil {
    static final int T7E = 0x7e;
    static final int T7D = 0x7d;
    static final int T01 = 0x01;
    static final int T02 = 0x02;
    static DecimalFormat decimalFormat = new DecimalFormat("#0.00");
    static DecimalFormat decimalFormatSix = new DecimalFormat("#0.000000");
    static DateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
    static DateFormat dateFormatCST = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
    static Pattern pattern = Pattern.compile("\\d+\\.?\\d*");

    public static byte toBYTE(int n) {
        return (byte) n;
    }

    public static byte[] toBYTES(List<Byte> data) {
        byte[] bytes = new byte[data.size()];
        for (int i = 0; i < data.size(); i++) {
            bytes[i] = data.get(i);
        }
        return bytes;
    }

    public static byte[] toBYTES(byte[] data, int start, int end) {
        byte[] bytes = new byte[end - start];
        for (int i = start; i < end; i++) {
            bytes[i - start] = data[i];
        }
        return bytes;
    }

    public static byte[] toBYTES(String s) {
        int len = s.length() / 8;
        byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++) {
            bytes[i] = toBYTE(Integer.valueOf(s.substring(i * 8, (i + 1) * 8), 2));
        }
        return bytes;
    }

    public static byte[] toWORD(int n) {
        short v = (short) n;
        return new byte[]{(byte) ((v >> 8) & 0xff ), (byte) (v & 0xff)};
    }

    public static byte[] toWORD(double n) {
        Double d = Double.parseDouble(decimalFormat.format(n)) * 10;
        return toWORD(d.intValue());
    }

    public static byte[] toDWORD(int n) {
        return new byte[]{(byte) ((n >> 24) & 0xff ), (byte) ((n >> 16) & 0xff ), (byte) ((n >> 8 ) & 0xff), (byte) (n & 0xff)};
    }

    public static byte[] toDWORD(double n) {
        Double d = Double.parseDouble(decimalFormat.format(n)) * 10;
        return toDWORD(d.intValue());
    }

    public static byte[] toDDWORD(long n) {
        return new byte[]{(byte) ((n >> 56) & 0xff), (byte) ((n >> 48) & 0xff), (byte) ((n >> 40) & 0xff), (byte) ((n >> 32) & 0xff), (byte) ((n >> 24) & 0xff ), (byte) ((n >> 16) & 0xff), (byte) ((n >> 8) & 0xff ), (byte) (n & 0xff)};
    }

    public static byte[] toBCD(String str) {
        if (str == null || str.trim().equals("")) {
            return new byte[]{};
        }
        if(str.length() % 2 == 1) {
            return HexUtil.decode("0" + str);
        }
        return HexUtil.decode(str);
    }

    public static String toReverBCD(List<Byte> list) {
        return toReverBCD(toBYTES(list));
    }

    public static String toReverBCD(byte[] bytes) {
        StringBuilder s = new StringBuilder();
        for (byte b : bytes) {
            String binaryStr = Integer.toBinaryString(byteToInt(b));
            StringBuilder sub = new StringBuilder();
            for (int i = 0; i < (8 - binaryStr.length()); i++) {
                sub.append("0");
            }
            sub.append(binaryStr);
            s.append(sub.toString());
        }
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < s.toString().length() / 4; i++) {
            int n = bcdToDEC(s.substring(i * 4, (i + 1) * 4));
            str.append(n);
        }
        return str.toString();
    }

    public static byte[] toSTRING(String str) {
        try {
            return str.getBytes("GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public static byte[] toSTRING(String str, int digit) {
        try {
            byte[] bytes = new byte[digit];
            byte[] b = str.getBytes("GBK");
            if (b.length < digit) {
                for (int i = 0; i < digit - b.length; i++) {
                    bytes[i] = toBYTE(0);
                }
                for (int i = 0; i < b.length; i++) {
                    int index = i + digit - b.length;
                    bytes[index] = b[i];
                }
            } else if (b.length > digit) {
                for (int i = b.length - digit; i < b.length; i++) {
                    int index = i - (b.length - digit);
                    bytes[index] = b[i];
                }
            } else {
                for (int i = 0; i < b.length; i++) {
                    bytes[i] = b[i];
                }
            }
            return bytes;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] bytes = new byte[digit];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = toBYTE(0);
        }
        return bytes;
    }

    public static byte[] toBCDByDateTime(Date date) {
        DateFormat fmt = new SimpleDateFormat("yyMMddHHmmss");
        return toBCD(fmt.format(date));
    }

    public static byte[] toLatLon(double n) {
        Double d = Double.parseDouble(decimalFormatSix.format(n)) * 1000000;
        return toDWORD(d.intValue());
    }

    public static byte[] toTrans(List<Byte> msg) {
        List<Byte> list = new ArrayList<>();
        list.add(toBYTE(T7E));
        for (int i = 0; i < msg.size(); i++) {
            if (T7E == msg.get(i)) {
                list.add(toBYTE(T7D));
                list.add(toBYTE(T02));
            } else if (T7D == msg.get(i)) {
                list.add(toBYTE(T7D));
                list.add(toBYTE(0x01));
            } else {
                list.add(msg.get(i));
            }
        }
        list.add(toBYTE(T7E));
        return toBYTES(list);
    }

    public static byte[] toTrans(byte[] msgHead, byte[] msgContent, byte checkCode) {
        List<Byte> list = new ArrayList<>();
        for (byte b : msgHead) {
            list.add(b);
        }
        for (byte b : msgContent) {
            list.add(b);
        }
        list.add(checkCode);
        return toTrans(list);
    }

    public static int toDirection(String str) {
        double angle = 0;
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            final double n = Double.parseDouble(matcher.group());
            if (angle < n) {
                angle = n;
            }
        }
        double n;
        if (str.contains("正北")) {
            n = 0;
        } else if (str.contains("东北")) {
            n = 45;
        } else if (str.contains("东偏北")) {
            n = angle;
        } else if (str.contains("北偏东")) {
            n = 90 - angle;
        } else if (str.contains("正东")) {
            n = 90;
        } else if (str.contains("东南")) {
            n = 135;
        } else if (str.contains("东偏南")) {
            n = 90 + angle;
        } else if (str.contains("南偏东")) {
            n = 180 - angle;
        } else if (str.contains("正南")) {
            n = 180;
        } else if (str.contains("南偏西")) {
            n = 180 + angle;
        } else if (str.contains("西南")) {
            n = 225;
        } else if (str.contains("西偏南")) {
            n = 270 - angle;
        } else if (str.contains("正西")) {
            n = 270;
        } else if (str.contains("西北")) {
            n = 315;
        } else if (str.contains("西偏北")) {
            n = 270 + angle;
        } else if (str.contains("北偏西")) {
            n = 360 - angle;
        } else {
            n = 0;
        }
        return (int) n;
    }

    private static int bcdToDEC(String bcd) {
        switch (bcd) {
            case "0000":
                return 0;
            case "0001":
                return 1;
            case "0010":
                return 2;
            case "0011":
                return 3;
            case "0100":
                return 4;
            case "0101":
                return 5;
            case "0110":
                return 6;
            case "0111":
                return 7;
            case "1000":
                return 8;
            case "1001":
                return 9;
        }
        throw new Error("8421码转十进制错误");
    }


    private static String decToBCD(int n) {
        switch (n) {
            case 1:
                return "0001";
            case 2:
                return "0010";
            case 3:
                return "0011";
            case 4:
                return "0100";
            case 5:
                return "0101";
            case 6:
                return "0110";
            case 7:
                return "0111";
            case 8:
                return "1000";
            case 9:
                return "1001";
            default:
                return "0000";
        }
    }

    public static byte[] dateToBCD(String dateTimeStr) {
        Date date;
        try {
            date = dateFormatCST.parse(dateTimeStr);
        } catch (ParseException e) {
            e.printStackTrace();
            date = new Date();
        }
        StringBuilder s = new StringBuilder();
        String str = dateFormat.format(date);
        for (int i = 0; i < str.length(); i++) {
            int n = Character.getNumericValue(str.charAt(i));
            s.append(decToBCD(n));
        }
        return toBYTES(s.toString());
    }

    /**
     * 反转义
     *
     * @param bytes
     * @return
     */
    public static byte[] toRever(byte[] bytes) {
        List<Byte> msg = new ArrayList<>();
        for (byte b : bytes) {
            msg.add(b);
        }
        return toRever(msg);
    }

    public static byte[] toRever(List<Byte> msg) {
        List<Byte> list = new ArrayList<>();
        int t7eCnt = 0;
        int i = 0;
        while (i < msg.size()) {
            if (T7E == msg.get(i)) {
                t7eCnt++;
                if (t7eCnt == 2) {
                    break;
                }
            } else {
                if (T7D == msg.get(i)) {
                    if (i + 1 < msg.size()) {
                        byte nextByte = msg.get(i + 1);
                        if (T01 == nextByte) {
                            list.add(toBYTE(T7D));
                            i += 2;
                            continue;
                        } else if (T02 == nextByte) {
                            list.add(toBYTE(T7E));
                            i += 2;
                            continue;
                        } else {
                            list.add(msg.get(i));
                        }
                    } else {
                        list.add(msg.get(i));
                    }
                } else {
                    list.add(msg.get(i));
                }
            }
            i++;
        }
        return toBYTES(list);
    }

    public static List<byte[]> toReverMulti(byte[] bytes) {
        List<Byte> msg = new ArrayList<>();
        for (byte b : bytes) {
            msg.add(b);
        }
        return toReverMulti(msg);
    }

    /// 反转义
    public static List<byte[]> toReverMulti(List<Byte> msg) {
        String str = HexUtil.encode(toBYTES(msg));
        List<byte[]> arr = new ArrayList<>();
        int t7eCnt = 0;
        List<Byte> data = new ArrayList<>();
        for (int i = 0; i < str.length(); i += 2) {
            int end = i + 2;
            if (end > str.length()) {
                end = str.length();
            }
            int e = Integer.parseInt(str.substring(i, end), 16);
            if (e == T7E) {
                t7eCnt++;
                if (t7eCnt % 2 == 1) {
                    data = new ArrayList<>();
                    data.add(toBYTE(T7E));
                } else {
                    arr.add(toBYTES(data));
                    data.add(toBYTE(T7E));
                }
            } else {
                data.add(toBYTE(e));
            }
        }
        return arr;
    }

    public static byte toCheckCode(byte[] bytes) {
        List<Byte> list = new ArrayList<>();
        for (byte b : bytes) {
            list.add(b);
        }
        return toCheckCode(list);
    }

    public static byte toCheckCode(List<Byte> list) {
        byte checkCode = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            checkCode = (byte) (checkCode ^ list.get(i));
        }
        return checkCode;
    }

    public static byte toCheckCode(byte[] msgHead, byte[] msgContent) {
        List<Byte> list = new ArrayList<>();
        for (byte b : msgHead) {
            list.add(b);
        }
        for (byte b : msgContent) {
            list.add(b);
        }
        return toCheckCode(list);
    }

    public static int byteToInt(byte b) {
        byte[] bytes = new byte[1];
        bytes[0] = b;
        return bytesToInt(bytes);
    }

    public static int bytesToInt(byte[] bytes) {
        List<Byte> list = new ArrayList<>();
        for (int i = 0; i < 4 - bytes.length; i++) {
            list.add((byte) 0);
        }
        for (byte b : bytes) {
            list.add(b);
        }
        int a = (list.get(0) & 0xff) << 24;
        int b = (list.get(1) & 0xff) << 16;
        int c = (list.get(2) & 0xff) << 8;
        int d = (list.get(3) & 0xff);
        return a | b | c | d;
    }

    public static int bytesToInt(List<Byte> list) {
        byte[] bytes = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            bytes[i] = list.get(i);
        }
        return bytesToInt(bytes);
    }

    public static byte[] toWriteBytes(String code, MsgContent msgContent) {
        byte[] msgHead = new MsgHead(msgContent.getMsgId(), msgContent.getContent().length, code).toBytes();
        byte checkCode = toCheckCode(msgHead, msgContent.getContent());
        return toTrans(msgHead, msgContent.getContent(), checkCode);
    }

    public static byte[] toWriteBytes(String code, MsgContent msgContent, int serialNo) {
        byte[] msgHead = new MsgHead(msgContent.getMsgId(), msgContent.getContent().length, code, serialNo).toBytes();
        byte checkCode = toCheckCode(msgHead, msgContent.getContent());
        return toTrans(msgHead, msgContent.getContent(), checkCode);
    }

    public static byte[] toWriteBytes(String code, MsgContent msgContent, int serialNo, boolean divide, int msgTotal, int packageNo) {
        byte[] msgHead = new MsgHead(msgContent.getMsgId(), divide, msgContent.getContent().length, code, serialNo, msgTotal, packageNo).toBytes();
        byte checkCode = toCheckCode(msgHead, msgContent.getContent());
        return toTrans(msgHead, msgContent.getContent(), checkCode);
    }
}
