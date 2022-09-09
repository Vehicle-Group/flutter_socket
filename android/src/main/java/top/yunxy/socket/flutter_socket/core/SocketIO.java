package top.yunxy.socket.flutter_socket.core;

import android.os.Handler;

import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import top.yunxy.socket.flutter_socket.jtt.*;
import top.yunxy.socket.flutter_socket.util.DataTypeUtil;
import top.yunxy.socket.flutter_socket.util.HexUtil;

import android.os.Handler;
import android.os.Looper;

public class SocketIO {
    String ip;
    List<Integer> ports;
    String type;
    String code;
    boolean heartbeat;
    private Socket socket;
    private boolean authState = false;
    private boolean exit = false;
    private long lastSendInterval = 0;
    private int serialNo = 0;
    private long connectInterval = 0;
    private LinkedList messageQueue = new LinkedList<Message>();
    private HashMap messageRecord = new HashMap<Integer, Long>();
    private Lock messageQueueLock = new ReentrantLock();
    private Lock messageRecordLock = new ReentrantLock();
    private TimeLock timeLock = new TimeLock();
    private Event streamEvent;
    private final int bufSize = 450;
    private final Handler handler;
    //media
    private Lock mediaLock = new ReentrantLock();
    private HashMap mediaMap = new HashMap<String, HashMap<Integer, Message>>();

    public SocketIO(String ip, List<Integer> ports, String type, String code, boolean heartbeat) {
        this.ip = ip;
        this.ports = ports;
        this.type = type;
        this.code = code;
        this.heartbeat = heartbeat;
        this.handler = new Handler(Looper.getMainLooper());
        this.connect();
        this.sendHandle();
        this.runHeartbeat();
    }

    public SocketIO(String ip, List<Integer> ports, String type, String code, boolean heartbeat, Event streamEvent) {
        this.ip = ip;
        this.ports = ports;
        this.type = type;
        this.code = code;
        this.heartbeat = heartbeat;
        this.streamEvent = streamEvent;
        this.handler = new Handler(Looper.getMainLooper());
        this.connect();
        this.sendHandle();
        this.runHeartbeat();
    }

    public void connect() {
        debug("connect", ip, ports.toString());
        connectInterval = System.currentTimeMillis();
        new Thread(() -> {
            int wait = 3000;
            int pi = 0;
            while (!exit) {
                try {
                    socket = new Socket(ip, ports.get(pi));
                    if (socket != null && socket.isConnected()) {
                        call("connect", true);
                        lastSendInterval = 0;
                        dataHandle();
                        authentication();
                        break;
                    } else {
                        pi = (pi + 1) % ports.size();
                        Thread.sleep(wait);
                    }
                } catch (Exception e) {
                    pi = (pi + 1) % ports.size();
                    try {
                        Thread.sleep(wait);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }).start();
    }

    private synchronized void call(String type, Object content) {
        HashMap data = new HashMap<String, Object>();
        data.put("type", type);
        data.put("content", content);
        handler.post(() -> {
            if (streamEvent != null) {
                Gson gson = new Gson();
                streamEvent.call(gson.toJson(data));
            }
        });
    }

    private void send(Message message) {
        if (exit) {
            return;
        }
        new Thread(() -> {
            messageQueueLock.lock();
            messageQueue.addFirst(message);
            messageQueueLock.unlock();
        }).start();
    }

    public synchronized void send(int msgId, String json) {
        MsgContent msgContent = null;
        Gson gson = new Gson();
        switch (msgId) {
            case 0x0200:
                T0200 t0200 = gson.fromJson(json, T0200.class);
                msgContent = t0200.toContent();
                break;
            case 0x0F01:
                T0F01 t0F01 = gson.fromJson(json, T0F01.class);
                msgContent = t0F01.toContent();
                break;
            case 0x0805:
                T0805 t0805 = gson.fromJson(json, T0805.class);
                msgContent = t0805.toContent();
                break;
            default:
                debug("unknown msg", MessageType.get(msgId).toString());
        }
        if (msgContent == null) {
            return;
        }
        final int serNo = nextSerialNo();
        byte[] data = DataTypeUtil.toWriteBytes(code, msgContent, serNo);
        send(new Message(msgId, data, serNo));
    }

    public synchronized void sendMedia(String t0800Json, String t0200Json, byte[] bytes) {
        Gson gson = new Gson();
        T0800 t0800 = gson.fromJson(t0800Json, T0800.class);
        T0200 t0200 = gson.fromJson(t0200Json, T0200.class);
        List<Byte> data = new ArrayList<>();
        for (byte b : bytes) {
            data.add(b);
        }
        int total = (int) Math.ceil((data.size() * 1.0 / bufSize));
        int serNo = nextSerialNo(total + 1);
        //T0800
        byte[] t0800Data = DataTypeUtil.toWriteBytes(code, t0800.toContent(), serNo);
        send(new Message(0x0800, t0800Data, serNo));
        serNo++;
        //send 0801
        int mediaId = t0800.getId();
        String key = mediaId + "-" + serNo;
        Map<Integer, Message> messageMap = new HashMap<>();
        for (int i = 0; i < total; i++) {
            int start = i * bufSize;
            int end = (i + 1) * bufSize;
            if (end > data.size()) {
                end = data.size();
            }
            List<Byte> subData = data.subList(start, end);
            MsgContent msgContent;
            if (i == 0) {
                msgContent = new T0801(mediaId, 0, 0, 0, 1, t0200, DataTypeUtil.toBYTES(subData)).toContent();
            } else {
                msgContent = new MsgContent(0x0801, DataTypeUtil.toBYTES(subData));
            }
            byte[] realData = DataTypeUtil.toWriteBytes(code, msgContent, (serNo + i) % 65535, total != 1, total, i + 1);
            Message message = new Message(0x0801, realData, (serNo + i) % 65535, true, total, i + 1);
            messageMap.put(i + 1, message);
            send(message);
        }
        mediaLock.lock();
        mediaMap.put(key, messageMap);
        mediaLock.unlock();
    }

    public void resendMulti(int serNo, List<Integer> ids) {
        mediaLock.lock();
        for (Object o : mediaMap.keySet()) {
            String key = (String) o;
            if (!key.contains("-" + serNo)) {
                continue;
            }
            if (ids.isEmpty()) {
                mediaMap.remove(key);
                break;
            }
            for (Integer id : ids) {
                HashMap<Integer, Message> messageMap = (HashMap<Integer, Message>) mediaMap.get(key);
                send(messageMap.get(id));
            }
            break;
        }
        mediaLock.unlock();
    }

    public void resendMedia(int mediaId, List<Integer> ids) {
        mediaLock.lock();
        for (Object o : mediaMap.keySet()) {
            String key = (String) o;
            if (!key.contains(mediaId + "-")) {
                continue;
            }
            if (ids.isEmpty()) {
                mediaMap.remove(key);
                break;
            }
            for (Integer id : ids) {
                HashMap<Integer, Message> messageMap = (HashMap<Integer, Message>) mediaMap.get(key);
                send(messageMap.get(id));
            }
            break;
        }
        mediaLock.unlock();
    }

    public void close() {
        exit = true;
        authState = false;
        try {
            timeLock.lock(3000);
            logout();
        } catch (InterruptedException e) {
            timeLock.unlock();
            closeSocket();
        }
    }

    public boolean getConnectState() {
        return socket != null && socket.isConnected();
    }

    public boolean getAuthState() {
        return authState;
    }

    private int nextSerialNo() {
        return nextSerialNo(1);
    }

    private synchronized int nextSerialNo(int size) {
        final int next = serialNo % 65535;
        serialNo += size;
        return next;
    }

    private void sendHandle() {
        new Thread(() -> {
            int wait = 500;
            OutputStream out = null;
            while (!exit) {
                if (!getConnectState()) {
                    try {
                        Thread.sleep(wait);
                    } catch (InterruptedException e) {
                    }
                    continue;
                }
                try {
                    out = socket.getOutputStream();
                    while (getConnectState()) {
                        if (!authState || emptyMessageQueue()) {
                            Thread.sleep(wait);
                            continue;
                        }
                        Message message = getLastMessageQueue();
                        if (existMessageRecord(message.getSerialNo()) && System.currentTimeMillis() < (getMessageRecord(message.getSerialNo()) + 5000)) {
                            Thread.sleep(wait);
                            continue;
                        }
                        try {
                            timeLock.lock(5000);
                            if(!getConnectState()) {
                                continue;
                            }
                            putMessageRecord(message.getSerialNo());
                            debug("<<<-", message.toString());
                            out.write(message.getData());
                            out.flush();
                            lastSendInterval = System.currentTimeMillis();
                        } catch (InterruptedException e) {
                            debug("超时未响应");
                            errorHandle();
                            timeLock.unlock();
                        }
                    }
                } catch (Exception e) {
                    errorHandle();
                } finally {
                    try {
                        out.close();
                    } catch (Exception e) {
                    }
                }
            }
        }).start();
    }

    private void dataHandle() {
        new Thread(() -> {
            InputStream in = null;
            byte[] buf = new byte[512];
            try {
                in = socket.getInputStream();
                while (!exit && getConnectState()) {
                    int len = in.read(buf);
                    if (len == -1) {
                        continue;
                    }
                    debug("recv origin hex", HexUtil.encode(buf).replaceAll("0*$", ""));
                    timeLock.unlock();
                    final List<byte[]> multiData = DataTypeUtil.toReverMulti(buf);
                    for (byte[] oData : multiData) {
                        byte[] data = DataTypeUtil.toRever(oData);
                        final MsgHead msgHead = new MsgHead(data);
                        final int msgHeadLen = msgHead.toBytes().length;
                        byte[] body = DataTypeUtil.toBYTES(data, msgHeadLen, msgHeadLen + msgHead.getMsgLen());
                        dataHandleMessage(msgHead, body, HexUtil.encode(oData));
                    }
                }
            } catch (Exception e) {
                errorHandle();
            } finally {
                try {
                    in.close();
                } catch (Exception e) {
                }
            }
        }).start();
    }

    private void dataHandleMessage(MsgHead msgHead, byte[] body, String hex) {
        switch (msgHead.getMsgId()) {
            case 0x8001:
                final T8001 t8001 = new T8001(body);
                debug("->>>", MessageType.get(msgHead.getMsgId()).toString(), t8001.toString(), hex);
                if (!emptyMessageQueue()) {
                    if (existMessageRecord(t8001.getAnswerSerialNo())) {
                        removeMessageRecord(t8001.getAnswerSerialNo());
                        removeLastMessageQueue();
                    }
                }
                if (t8001.getAnswerId() == 0x0102 && t8001.getResult() == 0) {
                    authState = true;
                    call("auth", true);
                }
                if (t8001.getAnswerId() == 0x0003 && t8001.getResult() == 0) {
                    closeSocket();
                }
//                streamHandle(t8001, msgHead.getSerialNo());
                break;
            case 0x8003:
                final T8003 t8003 = new T8003(body);
                debug("->>>", MessageType.get(msgHead.getMsgId()).toString(), t8003.toString(), hex);
                resendMulti(t8003.getSerialNo(), t8003.getIds());
                streamHandle(t8003, msgHead.getSerialNo());
                break;
            case 0x8800:
                final T8800 t8800 = new T8800(body);
                debug("->>>", MessageType.get(msgHead.getMsgId()).toString(), t8800.toString(), hex);
                resendMedia(t8800.getId(), t8800.getIds());
                streamHandle(t8800, msgHead.getSerialNo());
                break;
            case 0x8801:
                final T8801 t8801 = new T8801(body);
                debug("->>>", MessageType.get(msgHead.getMsgId()).toString(), t8801.toString(), hex);
                streamHandle(t8801, msgHead.getSerialNo());
                break;
            case 0x8803:
                final T8803 t8803 = new T8803(body);
                debug("->>>", MessageType.get(msgHead.getMsgId()).toString(), t8803.toString(), hex);
                streamHandle(t8803, msgHead.getSerialNo());
                break;
            case 0x9101:
                final T9101 t9101 = new T9101(body);
                sendAnswer(msgHead);
                debug("->>>", MessageType.get(msgHead.getMsgId()).toString(), t9101.toString(), hex);
                streamHandle(t9101, msgHead.getSerialNo());
                break;
            case 0x9102:
                final T9102 t9102 = new T9102(body);
                debug("->>>", MessageType.get(msgHead.getMsgId()).toString(), t9102.toString(), hex);
                streamHandle(t9102, msgHead.getSerialNo());
                break;
            default:
                sendAnswer(msgHead);
                break;
        }
    }

    private void sendAnswer(MsgHead msgHead) {
        final int serNo = nextSerialNo();
        byte[] data = DataTypeUtil.toWriteBytes(code, new T0001(msgHead.getSerialNo(), msgHead.getMsgId(), 0).toContent(), serNo);
        send(new Message(0x0001, data, serNo));
    }

    private void streamHandle(Object obj, int serialNo) {
        Gson gson = new Gson();
        String json = gson.toJson(obj);
        Map<String, Object> map = gson.fromJson(json, Map.class);
        map.put("code", code);
        map.put("socketType", type);
        map.put("serialNo", serialNo);
        call("event", obj);
    }

    private void debug(String... args) {
        StringBuilder sb = new StringBuilder(code + " [" + type + "] ");
        for (String arg : args) {
            sb.append(arg + " ");
        }
        call("debug", sb.toString());
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
        } finally {
            socket = null;
        }
    }

    private void authentication() {
        try {
            OutputStream os = socket.getOutputStream();
            T0102 t0102 = new T0102(code);
            byte[] data = DataTypeUtil.toWriteBytes(code, t0102.toContent(), nextSerialNo());
            os.write(data);
            os.flush();
            lastSendInterval = System.currentTimeMillis();
            debug("<<<-", MessageType.get(0x0102).toString(), HexUtil.encode(data));
        } catch (Exception e) {
        }
    }

    private void logout() {
        if(!getConnectState()) {
            return;
        }
        try {
            OutputStream os = socket.getOutputStream();
            T0003 t0003 = new T0003();
            byte[] data = DataTypeUtil.toWriteBytes(code, t0003.toContent(), nextSerialNo());
            os.write(data);
            os.flush();
            lastSendInterval = System.currentTimeMillis();
            debug("<<<-", MessageType.get(0x0003).toString(), HexUtil.encode(data));
        } catch (Exception e) {
        }
    }

    private synchronized void errorHandle() {
        if (System.currentTimeMillis() - connectInterval < 10000 || !authState) {
            return;
        }
        try {
            socket.close();
        } catch (Exception e) {
        } finally {
            socket = null;
        }
        authState = false;
        call("connect", false);
        call("auth", false);
        connect();
    }

    private void runHeartbeat() {
        new Thread(() -> {
            int wait = 2500;
            while (!exit && heartbeat) {
                long timestamp = System.currentTimeMillis();
                if (!getConnectState() || !authState || lastSendInterval == 0 || timestamp - lastSendInterval < wait) {
                    try {
                        Thread.sleep(wait);
                    } catch (InterruptedException e) {
                    }
                    continue;
                }
                lastSendInterval = timestamp;
                final int serNo = nextSerialNo();
                byte[] data = DataTypeUtil.toWriteBytes(code, new T0002().toContent(), serNo);
                send(new Message(0x0002, data, serNo));
            }
        }).start();
    }

    private boolean emptyMessageQueue() {
        messageQueueLock.lock();
        boolean empty = messageQueue.isEmpty();
        messageQueueLock.unlock();
        return empty;
    }

    private Message getLastMessageQueue() {
        messageQueueLock.lock();
        Message message = (Message) messageQueue.getLast();
        messageQueueLock.unlock();
        return message;
    }

    private Message removeLastMessageQueue() {
        messageQueueLock.lock();
        Message message = (Message) messageQueue.removeLast();
        messageQueueLock.unlock();
        return message;
    }

    private boolean existMessageRecord(int serNo) {
        messageRecordLock.lock();
        boolean exist = messageRecord.containsKey(serNo);
        messageRecordLock.unlock();
        return exist;
    }

    private long getMessageRecord(int serNo) {
        messageRecordLock.lock();
        long v;
        if (!messageRecord.containsKey(serNo)) {
            v = System.currentTimeMillis();
        } else {
            v = (long) messageRecord.get(serNo);
        }
        messageRecordLock.unlock();
        return v;
    }

    private void putMessageRecord(int serNo) {
        messageRecordLock.lock();
        messageRecord.put(serNo, System.currentTimeMillis());
        messageRecordLock.unlock();
    }

    private void removeMessageRecord(int serNo) {
        messageRecordLock.lock();
        messageRecord.remove(serNo);
        messageRecordLock.unlock();
    }
}
