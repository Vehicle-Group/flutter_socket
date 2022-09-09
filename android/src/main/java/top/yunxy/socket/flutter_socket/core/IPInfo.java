package top.yunxy.socket.flutter_socket.core;

import java.util.List;

public class IPInfo {

    String ip;
    List<Integer> ports;
    String type;

    public IPInfo() {
    }

    public IPInfo(String ip, List<Integer> ports, String type) {
        this.ip = ip;
        this.ports = ports;
        this.type = type;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public List<Integer> getPorts() {
        return ports;
    }

    public void setPorts(List<Integer> ports) {
        this.ports = ports;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "IPInfo{" +
                "ip='" + ip + '\'' +
                ", ports=" + ports +
                ", type='" + type + '\'' +
                '}';
    }
}
