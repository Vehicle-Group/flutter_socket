package top.yunxy.socket.flutter_socket.jtt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import top.yunxy.socket.flutter_socket.util.DataTypeUtil;

/**
 * 位置信息汇报
 */
public class T0200 extends Base {
    //消息Id
    private int msgId = 0x0200;
    //报警(默认：0   进出区域：1 << 20)
    private int alarm;
    //状态
    private int state;
    //纬度
    private double latitude;
    //经度
    private double longitude;
    //高程（海拔）
    private int altitude;
    //速度
    private double speed;
    //方向
    private double direction;
    //时间
    private String time;
    //里程
    private double mileage;

    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public T0200() {
    }

    public T0200(int alarm, int state, double latitude, double longitude, int altitude, double speed, double direction, String time, double mileage) {
        this.alarm = alarm;
        this.state = state;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.speed = speed;
        this.direction = direction;
        this.time = time;
        this.mileage = mileage;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public int getAlarm() {
        return alarm;
    }

    public void setAlarm(int alarm) {
        this.alarm = alarm;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getAltitude() {
        return altitude;
    }

    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getDirection() {
        return direction;
    }

    public void setDirection(double direction) {
        this.direction = direction;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getMileage() {
        return mileage;
    }

    public void setMileage(double mileage) {
        this.mileage = mileage;
    }

    public SimpleDateFormat getFormatter() {
        return formatter;
    }

    @Override
    public byte[] toBytes() {
        List<Byte> list = new ArrayList<>();
        //报警
        for (byte b : DataTypeUtil.toDWORD(this.alarm)) {
            list.add(b);
        }
        //状态
        for (byte b : DataTypeUtil.toDWORD(this.state)) {
            list.add(b);
        }
        //纬度
        for (byte b : DataTypeUtil.toLatLon(this.latitude)) {
            list.add(b);
        }
        //经度
        for (byte b : DataTypeUtil.toLatLon(this.longitude)) {
            list.add(b);
        }
        //高程
        for (byte b : DataTypeUtil.toWORD(this.altitude)) {
            list.add(b);
        }
        //速度
        for (byte b : DataTypeUtil.toWORD(this.speed)) {
            list.add(b);
        }
        //方向
        for (byte b : DataTypeUtil.toWORD(this.direction)) {
            list.add(b);
        }
        //时间
        try {
            for (byte b : DataTypeUtil.toBCDByDateTime(formatter.parse(this.time))) {
                list.add(b);
            }
        } catch (Exception e) {
            for (byte b : DataTypeUtil.toBCDByDateTime(new Date(System.currentTimeMillis()))) {
                list.add(b);
            }
        }
        //里程
        list.add(DataTypeUtil.toBYTE(0x01));
        list.add(DataTypeUtil.toBYTE(4));
        for (byte b : DataTypeUtil.toDWORD(this.mileage)) {
            list.add(b);
        }

        return DataTypeUtil.toBYTES(list);
    }

    @Override
    public MsgContent toContent() {
        return new MsgContent(msgId, toBytes());
    }

    public MsgContent toContent(boolean keep28) {
        return new MsgContent(msgId, DataTypeUtil.toBYTES(toBytes(), 0, 28));
    }

    @Override
    public String toString() {
        return "T0200{" +
                ", alarm=" + alarm +
                ", state=" + state +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", altitude=" + altitude +
                ", speed=" + speed +
                ", direction=" + direction +
                ", time='" + time + '\'' +
                ", mileage=" + mileage +
                '}';
    }
}
