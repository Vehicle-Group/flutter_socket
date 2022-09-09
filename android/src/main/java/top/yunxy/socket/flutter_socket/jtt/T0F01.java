package top.yunxy.socket.flutter_socket.jtt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import top.yunxy.socket.flutter_socket.util.DataTypeUtil;

/**
 * 车辆状态上报
 */
public class T0F01 extends Base {
    //消息id
    private int msgId = 0x0F01;
    //经度
    private double longitude;
    //纬度
    private double latitude;
    //高程（海拔）
    private int altitude;
    //速度
    private double speed;
    //方向
    private double direction;
    //时间
    private String time;
    //车牌号码
    private String licensePlate;
    //司机Id
    private String driverId;
    //车厢状态
    private String carriageState;
    //举升状态
    private String liftState;
    //空重状态
    private String emptyWeightState;

    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public T0F01() {
    }

    public T0F01(double longitude, double latitude, int altitude, double speed, double direction, String time, String licensePlate, String driverId, String carriageState, String liftState, String emptyWeightState) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
        this.speed = speed;
        this.direction = direction;
        this.time = time;
        this.licensePlate = licensePlate;
        this.driverId = driverId;
        this.carriageState = carriageState;
        this.liftState = liftState;
        this.emptyWeightState = emptyWeightState;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
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

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getCarriageState() {
        return carriageState;
    }

    public void setCarriageState(String carriageState) {
        this.carriageState = carriageState;
    }

    public String getLiftState() {
        return liftState;
    }

    public void setLiftState(String liftState) {
        this.liftState = liftState;
    }

    public String getEmptyWeightState() {
        return emptyWeightState;
    }

    public void setEmptyWeightState(String emptyWeightState) {
        this.emptyWeightState = emptyWeightState;
    }

    public SimpleDateFormat getFormatter() {
        return formatter;
    }

    @Override
    public byte[] toBytes() {
        List<Byte> list = new ArrayList<>();
        //状态
        for (byte b : DataTypeUtil.toWORD(0)) {
            list.add(b);
        }
        //经度
        for (byte b : DataTypeUtil.toLatLon(this.longitude)) {
            list.add(b);
        }
        //纬度
        for (byte b : DataTypeUtil.toLatLon(this.latitude)) {
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
        //车牌号码
        for (byte b : DataTypeUtil.toSTRING(this.licensePlate, 8)) {
            list.add(b);
        }
        //司机ID
        for (byte b : DataTypeUtil.toSTRING(this.driverId, 10)) {
            list.add(b);
        }
        //保留
        for (int i = 0; i < 10; i++) {
            list.add(DataTypeUtil.toBYTE(0));
        }
        //车厢状态
        if (this.carriageState.indexOf("关闭") != -1) {
            list.add(DataTypeUtil.toBYTE(1));
        } else if (this.carriageState.indexOf("打开") != -1) {
            list.add(DataTypeUtil.toBYTE(2));
        } else if (this.carriageState.indexOf("故障") != -1) {
            list.add(DataTypeUtil.toBYTE(0x0a));
        } else {
            list.add(DataTypeUtil.toBYTE(1));
        }
        //举升状态
        if (this.liftState.indexOf("平放") != -1) {
            list.add(DataTypeUtil.toBYTE(1));
        } else if (this.liftState.indexOf("举升") != -1) {
            list.add(DataTypeUtil.toBYTE(2));
        } else if (this.liftState.indexOf("完全举升") != -1) {
            list.add(DataTypeUtil.toBYTE(3));
        } else if (this.liftState.indexOf("故障") != -1) {
            list.add(DataTypeUtil.toBYTE(0x0a));
        } else {
            list.add(DataTypeUtil.toBYTE(1));
        }
        //空重状态
        if (this.emptyWeightState.indexOf("空车") != -1) {
            list.add(DataTypeUtil.toBYTE(1));
        } else if (this.emptyWeightState.indexOf("重车") != -1) {
            list.add(DataTypeUtil.toBYTE(2));
        } else if (this.emptyWeightState.indexOf("故障") != -1) {
            list.add(DataTypeUtil.toBYTE(0x0a));
        } else {
            list.add(DataTypeUtil.toBYTE(1));
        }
        //违规情况
        list.add(DataTypeUtil.toBYTE(0));

        return DataTypeUtil.toBYTES(list);
    }

    @Override
    public MsgContent toContent() {
        return new MsgContent(msgId, toBytes());
    }
}
