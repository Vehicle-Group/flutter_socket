package top.yunxy.socket.flutter_socket.core;

import android.os.Handler;

import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.util.*;

import top.yunxy.socket.flutter_socket.jtt.*;
import top.yunxy.socket.flutter_socket.util.DataTypeUtil;
import top.yunxy.socket.flutter_socket.util.HexUtil;

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
    private int serialNo = 0;
    private long connectInterval = 0;
    private long lastSendInterval = 0;
    private TimeLock timeLock = new TimeLock();
    private TimeLock mediaTimeLock = new TimeLock();
    private Event streamEvent;
    private final Handler handler;
    private RecordMessage recordMessage = new RecordMessage();
    private RecordMediaMessage recordMediaMessage = new RecordMediaMessage();

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
        call(type, content, serialNo);
    }

    private synchronized void call(String eventType, Object content, int serialNo) {
        HashMap data = new HashMap<String, Object>();
        data.put("type", eventType);
        data.put("content", content);
        data.put("code", code);
        data.put("socketType", type);
        data.put("serialNo", serialNo);
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
        recordMessage.add(message);
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
        recordMediaMessage.add(new MediaMessage(t0800, t0200, data));
    }

    public synchronized void resendMedia(int sign, List<Integer> ids, boolean isMedia) {
        if (!getConnectState()) {
            return;
        }
        try {
            OutputStream out = socket.getOutputStream();
            List<Message> messages = recordMediaMessage.retry(sign, ids, isMedia);
            for (Message message : messages) {
                debug("<<<- resend", message.toString());
                out.write(message.getData());
                out.flush();
            }
            lastSendInterval = System.currentTimeMillis();
        } catch (Exception e) {
        }
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
                        if (!authState) {
                            Thread.sleep(wait);
                            continue;
                        }

                        if (recordMessage.empty() && recordMediaMessage.next(code, serialNo, total -> nextSerialNo(total))) {
                            try {
                                mediaTimeLock.lock(20000);
                                if (!getConnectState()) {
                                    mediaTimeLock.unlock();
                                    continue;
                                }
                                List<Message> messages = recordMediaMessage.finds();
                                if(messages.size() == 0) {
                                    mediaTimeLock.unlock();
                                    continue;
                                }
                                for (Message message : messages) {
                                    debug("<<<-", message.toString());
                                    out.write(message.getData());
                                    out.flush();
                                }
                                lastSendInterval = System.currentTimeMillis();
                            } catch (InterruptedException e) {
                                debug("多媒体超时未响应", e.toString());
                                errorHandle();
                                mediaTimeLock.unlock();
                            } finally {
                                continue;
                            }
                        }


                        if (!recordMessage.next()) {
                            Thread.sleep(wait);
                            continue;
                        }
                        try {
                            timeLock.lock(5000);
                            if (!getConnectState()) {
                                timeLock.unlock();
                                continue;
                            }
                            Message message = recordMessage.find();
                            debug("<<<-", message.toString());
                            out.write(message.getData());
                            out.flush();
                            lastSendInterval = System.currentTimeMillis();
                        } catch (InterruptedException e) {
                            debug("超时未响应", e.toString());
                            errorHandle();
                            timeLock.unlock();
                        }
                    }
                } catch (Exception e) {
                    debug("sendHandle", e.toString());
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
            byte[] buf = new byte[10240];
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
                        try {
                            byte[] data = DataTypeUtil.toRever(oData);
                            if (data.length == 0) {
                                debug("rever data length 0");
                                continue;
                            }
                            final MsgHead msgHead = new MsgHead(data);
                            final int msgHeadLen = msgHead.toBytes().length;
                            byte[] body = DataTypeUtil.toBYTES(data, msgHeadLen, msgHeadLen + msgHead.getMsgLen());
                            dataHandleMessage(msgHead, body, HexUtil.encode(oData));
                        } catch (Exception e) {
                            debug("msgHead", e.toString());
                        }
                    }
                }
            } catch (Exception e) {
                debug("dataHandle", e.toString());
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
        Gson gson = new Gson();
        switch (msgHead.getMsgId()) {
            case 0x8001:
                final T8001 t8001 = new T8001(body);
                debug("->>>", MessageType.get(msgHead.getMsgId()).toString(), t8001.toString(), hex);
                recordMessage.remove(t8001.getAnswerSerialNo());
                if (t8001.getAnswerId() == 0x0102 && t8001.getResult() == 0) {
                    authState = true;
                    call("auth", true);
                }
                if (t8001.getAnswerId() == 0x0003 && t8001.getResult() == 0) {
                    closeSocket();
                }
                break;
            case 0x8003:
                final T8003 t8003 = new T8003(body);
                debug("->>>", MessageType.get(msgHead.getMsgId()).toString(), t8003.toString(), hex);
                resendMedia(t8003.getSerialNo(), t8003.getIds(), false);
                call("event", gson.toJson(t8003), msgHead.getSerialNo());
                break;
            case 0x8800:
                final T8800 t8800 = new T8800(body);
                debug("->>>", MessageType.get(msgHead.getMsgId()).toString(), t8800.toString(), hex);
                if (t8800.getIds().isEmpty()) {
                    mediaTimeLock.unlock();
                    sendAnswer(msgHead);
                }
                resendMedia(t8800.getId(), t8800.getIds(), true);
                call("event", gson.toJson(t8800), msgHead.getSerialNo());
                break;
            case 0x8801:
                final T8801 t8801 = new T8801(body);
                debug("->>>", MessageType.get(msgHead.getMsgId()).toString(), t8801.toString(), hex);
                call("event", gson.toJson(t8801), msgHead.getSerialNo());
                break;
            case 0x8803:
                final T8803 t8803 = new T8803(body);
                debug("->>>", MessageType.get(msgHead.getMsgId()).toString(), t8803.toString(), hex);
                call("event", gson.toJson(t8803), msgHead.getSerialNo());
                break;
            case 0x9101:
                final T9101 t9101 = new T9101(body);
                sendAnswer(msgHead);
                debug("->>>", MessageType.get(msgHead.getMsgId()).toString(), t9101.toString(), hex);
                call("event", gson.toJson(t9101), msgHead.getSerialNo());
                break;
            case 0x9102:
                final T9102 t9102 = new T9102(body);
                debug("->>>", MessageType.get(msgHead.getMsgId()).toString(), t9102.toString(), hex);
                call("event", gson.toJson(t9102), msgHead.getSerialNo());
                break;
            default:
                sendAnswer(msgHead);
                break;
        }
    }

    private void sendAnswer(MsgHead msgHead) {
        if (!getConnectState()) {
            return;
        }
        try {
            final int serNo = nextSerialNo();
            byte[] data = DataTypeUtil.toWriteBytes(code, new T0001(msgHead.getSerialNo(), msgHead.getMsgId(), 0).toContent(), serNo);
            Message message = new Message(0x0001, data, serNo);
            debug("<<<-", message.toString());
            OutputStream os = socket.getOutputStream();
            os.write(message.getData());
            os.flush();
            lastSendInterval = System.currentTimeMillis();
        } catch (Exception e) {
        }
    }

    private void debug(String... args) {
        StringBuilder sb = new StringBuilder(code + " [" + type + "] ");
        for (String arg : args) {
            sb.append(arg + " ");
        }
        System.out.println(sb.toString());
//        call("debug", sb.toString());
    }

    private void closeSocket() {
        if (!getConnectState()) {
            return;
        }
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
        if (!getConnectState()) {
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
                if (!getConnectState() || !authState || lastSendInterval == 0 || timestamp - lastSendInterval < wait || !recordMediaMessage.empty()) {
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
}
