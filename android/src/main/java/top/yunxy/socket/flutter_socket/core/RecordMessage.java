package top.yunxy.socket.flutter_socket.core;

import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RecordMessage {
    private LinkedList queue = new LinkedList<Message>();

    private Message curMessage = null;

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
        return curMessage;
    }

    public synchronized void remove(int serNo, Event event) {
        if(curMessage == null) {
            return;
        }
        if(curMessage.getSerialNo() == serNo) {
            curMessage = null;
            event.call("success");
        }
    }
}
