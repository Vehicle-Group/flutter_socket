package top.yunxy.socket.flutter_socket.core;

import java.util.List;

import top.yunxy.socket.flutter_socket.jtt.T0200;
import top.yunxy.socket.flutter_socket.jtt.T0800;

public class MediaMessage {
    private T0800 t0800;
    private T0200 t0200;
    private List<Byte> data;

    public MediaMessage(T0800 t0800, T0200 t0200, List<Byte> data) {
        this.t0800 = t0800;
        this.t0200 = t0200;
        this.data = data;
    }

    public T0800 getT0800() {
        return t0800;
    }

    public T0200 getT0200() {
        return t0200;
    }

    public List<Byte> getData() {
        return data;
    }
}
