package top.yunxy.socket.flutter_socket.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class RecordMessage {
    private LinkedList queue = new LinkedList<Message>();

    private Message curMessage = null;

    private Map messageCountMap = new HashMap<Integer, Integer>();

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
        if (messageCountMap.containsKey(curMessage.getMsgId())) {
            int cnt = (int) messageCountMap.get(curMessage.getMsgId());
            if(cnt > 2) {
                curMessage = null;
                return null;
            }
            messageCountMap.put(curMessage.getMsgId(), cnt + 1);
        } else {
            messageCountMap.put(curMessage.getMsgId(), 0);
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
            curMessage = null;
            if(event != null) {
                event.call("success");
            }
        }
    }
}
