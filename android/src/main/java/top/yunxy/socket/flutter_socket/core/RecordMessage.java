package top.yunxy.socket.flutter_socket.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class RecordMessage {
    private LinkedList queue = new LinkedList<Message>();

    private Message curMessage = null;

    private Map<Integer, Integer> messageCountMap = new HashMap<>();

    private Long lastSendInterval = 0L;

    public RecordMessage() {
    }

    public boolean empty() {
        return queue.size() == 0;
    }

    public synchronized void add(Message message) {
        queue.addFirst(message);
    }

    public synchronized boolean next() {
        if (curMessage != null && System.currentTimeMillis() < lastSendInterval + 5000) {
            return false;
        }
        if (curMessage == null) {
            if (queue.isEmpty()) {
                return false;
            }
            curMessage = (Message) queue.removeLast();
        }
        return true;
    }

    public synchronized Message find() {
        lastSendInterval = System.currentTimeMillis();
        if (messageCountMap.containsKey(curMessage.getSerialNo())) {
            int cnt = messageCountMap.get(curMessage.getSerialNo());
            if(cnt > 2) {
                messageCountMap.remove(curMessage.getSerialNo());
                curMessage = null;
                return null;
            }
            messageCountMap.put(curMessage.getSerialNo(), cnt + 1);
        } else {
            messageCountMap.put(curMessage.getSerialNo(), 0);
        }
        return curMessage;
    }

    public void remove(int serNo) {
        remove(serNo, null);
    }

    public synchronized void remove(int serNo, Event event) {
        if (curMessage == null) {
            return;
        }
        if (curMessage.getSerialNo() == serNo) {
            messageCountMap.remove(curMessage.getSerialNo());
            curMessage = null;
            if(event != null) {
                event.call("success");
            }
        }
    }
}
