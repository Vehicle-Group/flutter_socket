package top.yunxy.socket.flutter_socket.core;

import java.util.List;

public class SendMessage {
    private byte[] data;
    private List<Message> messages;

    public SendMessage(byte[] data, List<Message> messages) {
        this.data = data;
        this.messages = messages;
    }

    public byte[] getData() {
        return data;
    }

    public List<Message> getMessages() {
        return messages;
    }
}
