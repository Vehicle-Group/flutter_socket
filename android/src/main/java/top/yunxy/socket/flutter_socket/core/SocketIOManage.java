package top.yunxy.socket.flutter_socket.core;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SocketIOManage {
    private List<String> codes;
    private List<IPInfo> ipInfoList;
    private boolean video;
    private boolean heartbeat;
    private List<SocketIO> sockets;
    private Event streamEvent;
    private Lock lock = new ReentrantLock();

    public SocketIOManage(List<String> codes, List<IPInfo> ipInfoList, boolean video, boolean heartbeat) {
        this.codes = codes;
        this.ipInfoList = ipInfoList;
        this.video = video;
        this.heartbeat = heartbeat;
        init();
    }

    public SocketIOManage(List<String> codes, List<IPInfo> ipInfoList, boolean video, boolean heartbeat, Event streamEvent) {
        this.codes = codes;
        this.ipInfoList = ipInfoList;
        this.video = video;
        this.heartbeat = heartbeat;
        this.streamEvent = streamEvent;
        init();
    }

    private void init() {
            sockets = new ArrayList<>();
            for (String code : codes) {
                for (IPInfo ipInfo : ipInfoList) {
                    if (ipInfo.type.equals("video") && !video) {
                        continue;
                    }
                    SocketIO socketIO = new SocketIO(ipInfo.ip, ipInfo.ports, ipInfo.type, code, heartbeat, element -> {
                        lock.lock();
                        if(streamEvent != null) {
                            streamEvent.call(element);
                        }
                        lock.unlock();
                    });
                    lock.lock();
                    sockets.add(socketIO);
                    lock.unlock();
                }
            }
    }

    public void send(String code, String socketType, int msgId, String data) {
        lock.lock();
        for (SocketIO socket : sockets) {
            if(code != "" && !code.equals(socket.code)) {
                continue;
            }
            if(socketType != "" && !socketType.equals(socket.type)) {
                continue;
            }
            socket.send(msgId, data);
        }
        lock.unlock();
    }

    public void sendMedia(String code, String socketType, String t0800, String t0200, byte[] data) {
        lock.lock();
        for (SocketIO socket : sockets) {
            if(code != "" && !code.equals(socket.code)) {
                continue;
            }
            if(socketType != "" && !socketType.equals(socket.type)) {
                continue;
            }
            socket.sendMedia(t0800, t0200, data);
        }
        lock.unlock();
    }

    public void close() {
        lock.lock();
        for (SocketIO socket : sockets) {
            new Thread(() -> socket.close()).start();
        }
        lock.unlock();
    }
}
