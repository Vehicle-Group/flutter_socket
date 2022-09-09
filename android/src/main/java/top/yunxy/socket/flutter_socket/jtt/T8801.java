package top.yunxy.socket.flutter_socket.jtt;

public class T8801 {
    //消息id
    private int msgId = 0x8801;

    ///通道ID(大于0)
    private int channelId;

    ///拍摄命令：0表示停止拍摄；65535表示录像；其它表示拍照张数
    private int command;

    ///拍照间隔/录像时间(秒) 0表示按最小间隔拍照或一直录像
    private int time;

    ///保存标志：1.保存 0.实时上传
    int save;

    /// "分辨率：" +
    /// " 1.320*240" +
    /// " 2.640*480" +
    /// " 3.800*600" +
    /// " 4.1024*768" +
    /// " 5.176*144 [QCIF]" +
    /// " 6.352*288 [CIF]" +
    /// " 7.704*288 [HALF D1]" +
    /// " 8.704*576 [D1]"
    private int resolution;

    /// 图像/视频质量(1~10)：1.代表质量损失最小 10.表示压缩比最大
    private int quality;

    /// 亮度(0~255)
    private int brightness;

    /// 对比度(0~127)
    private int contrast;

    /// 饱和度(0~127)
    private int saturation;

    /// 色度(0~255)
    private int chroma;

    public T8801() {
    }

    public T8801(byte[] bytes) {
        channelId = bytes[0] & 0xff;
        command = (bytes[1] & 0xff) << 8 | (bytes[2] & 0xff);
        time = (bytes[3] & 0xff) << 8 | (bytes[4] & 0xff);
        save = bytes[5] & 0xff;
        resolution = bytes[6] & 0xff;
        quality = bytes[7] & 0xff;
        brightness = bytes[8] & 0xff;
        contrast = bytes[9] & 0xff;
        saturation = bytes[10] & 0xff;
        chroma = bytes[11] & 0xff;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getSave() {
        return save;
    }

    public void setSave(int save) {
        this.save = save;
    }

    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public int getContrast() {
        return contrast;
    }

    public void setContrast(int contrast) {
        this.contrast = contrast;
    }

    public int getSaturation() {
        return saturation;
    }

    public void setSaturation(int saturation) {
        this.saturation = saturation;
    }

    public int getChroma() {
        return chroma;
    }

    public void setChroma(int chroma) {
        this.chroma = chroma;
    }

    @Override
    public String toString() {
        return "T8801{" +
                "channelId=" + channelId +
                ", command=" + command +
                ", time=" + time +
                ", save=" + save +
                ", resolution=" + resolution +
                ", quality=" + quality +
                ", brightness=" + brightness +
                ", contrast=" + contrast +
                ", saturation=" + saturation +
                ", chroma=" + chroma +
                '}';
    }
}
