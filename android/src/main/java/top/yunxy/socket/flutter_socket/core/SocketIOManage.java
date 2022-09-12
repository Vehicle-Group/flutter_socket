package top.yunxy.socket.flutter_socket.core;


import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                    if (streamEvent == null) {
                        lock.unlock();
                        return;
                    }
                    Gson gson = new Gson();
                    HashMap<String, Object> map = gson.fromJson(element, HashMap.class);
                    if ("connect".equals(map.get("type"))) {
                        boolean f = true;
                        for (SocketIO socket : sockets) {
                            if(!socket.getConnectState()) {
                                f = false;
                                break;
                            }
                        }
                        map.put("content", f);
                        streamEvent.call(gson.toJson(map));
                    } else if ("currentIndex".equals(map.get("type"))) {
                        int currentIndex = Integer.MAX_VALUE;
                        for (SocketIO socket : sockets) {
                            if(currentIndex > socket.getCurrentIndex()) {
                                currentIndex = socket.getCurrentIndex();
                            }
                        }
                        map.put("content", currentIndex);
                        streamEvent.call(gson.toJson(map));
                    } else {
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
            if (!socket.getConnectState()) {
                continue;
            }
            if (!code.equals("") && !code.equals(socket.code)) {
                continue;
            }
            if (!socketType.equals("") && !socketType.equals(socket.type)) {
                continue;
            }
            socket.send(msgId, data);
        }
        lock.unlock();
    }

    public void sendMedia(String code, String socketType, String t0800, String t0200, byte[] data) {
        lock.lock();
        for (SocketIO socket : sockets) {
            if (!socket.getConnectState()) {
                continue;
            }
            if (!code.equals("") && !code.equals(socket.code)) {
                continue;
            }
            if (!socketType.equals("") && !socketType.equals(socket.type)) {
                continue;
            }
            socket.sendMedia(t0800, t0200, data);
        }
        lock.unlock();
    }

    public void close() {
        lock.lock();
        for (SocketIO socket : sockets) {
            if (socket == null) {
                continue;
            }
            new Thread(() -> socket.close()).start();
        }
        lock.unlock();
    }
}
