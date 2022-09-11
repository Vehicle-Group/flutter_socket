package top.yunxy.socket.flutter_socket.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import top.yunxy.socket.flutter_socket.jtt.MsgContent;
import top.yunxy.socket.flutter_socket.jtt.T0800;
import top.yunxy.socket.flutter_socket.jtt.T0801;
import top.yunxy.socket.flutter_socket.util.DataTypeUtil;

public class RecordMediaMessage {
    private final int bufSize = 450;

    private LinkedList queue = new LinkedList<MediaMessage>();

    private byte[] curData;

    private int curSerialNo = -1;

    private int curMediaId = -1;

    private Map<Integer, Message> curMediaMap = new HashMap<>();

    private Long lastSendInterval = 0L;

    private Message t0800Message;

    public RecordMediaMessage() {
    }

    public synchronized boolean empty() {
        return queue.size() == 0;
    }

    public synchronized void add(MediaMessage mediaMessage) {
        queue.addFirst(mediaMessage);
    }

    public synchronized boolean next(String code, int serNo, SerialNoEvent event) {
        if (curMediaId == -1) {
            if (queue.isEmpty()) {
                return false;
            }
            generate(code, serNo, event);
        }
        return true;
    }

    private synchronized void generate(String code, int serNo, SerialNoEvent event) {
        List<Byte> list = new ArrayList<>();
        MediaMessage media = (MediaMessage) queue.removeLast();
        int total = (int) Math.ceil((media.getData().size() * 1.0 / bufSize));
        event.call(total + 1);
        // 0800
        byte[] t0800Data = DataTypeUtil.toWriteBytes(code, media.getT0800().toContent(), serNo % 65535);
        t0800Message = new Message(0x0800, t0800Data, serNo % 65535);
        for (byte b : t0800Data) {
            list.add(b);
        }
        serNo++;
        //0801
        curSerialNo = serNo % 65535;
        curMediaId = media.getT0800().getId();
        for (int i = 0; i < total; i++) {
            int start = i * bufSize;
            int end = (i + 1) * bufSize;
            if (end > media.getData().size()) {
                end = media.getData().size();
            }
            List<Byte> subData = media.getData().subList(start, end);
            MsgContent msgContent;
            if (i == 0) {
                msgContent = new T0801(curMediaId, 0, 0, 0, media.getT0800().getChannelId(), media.getT0200(), DataTypeUtil.toBYTES(subData)).toContent();
            } else {
                msgContent = new MsgContent(0x0801, DataTypeUtil.toBYTES(subData));
            }
            byte[] realData = DataTypeUtil.toWriteBytes(code, msgContent, (serNo + i) % 65535, total != 1, total, i + 1);
            for (byte b : realData) {
                list.add(b);
            }
            Message message = new Message(0x0801, realData, (serNo + i) % 65535, true, total, i + 1);
            curMediaMap.put(i + 1, message);
        }
        curData = DataTypeUtil.toBYTES(list);
    }

    public synchronized byte[] find() {
        lastSendInterval = System.currentTimeMillis();
        return curData;
    }

    public synchronized List<Message> finds() {
        if (curMediaId == -1) {
            return new ArrayList<>();
        }
        lastSendInterval = System.currentTimeMillis();
        List<Message> data = new ArrayList<>();
        for (Integer key : curMediaMap.keySet()) {
            data.add(curMediaMap.get(key));
        }
        Collections.sort(data);
        data.add(0, t0800Message);
        return data;
    }

    public synchronized List<Message> retry(int sign, List<Integer> ids, boolean isMedia) {
        System.out.println("clear empty " + sign + " " + ids.isEmpty() + " " + isMedia + " ");
        if (isMedia && sign != curMediaId) {
            return new ArrayList<>();
        }
        if (!isMedia && sign != curSerialNo) {
            return new ArrayList<>();
        }
        if (ids.isEmpty()) {
            System.out.println("clear success");
            curSerialNo = -1;
            curMediaId = -1;
            curMediaMap.clear();
            return new ArrayList<>();
        }
        List<Message> data = new ArrayList<>();
        for (Integer id : ids) {
            Message message = curMediaMap.get(id);
            data.add(message);
        }
        return data;
    }
}
