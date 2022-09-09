import 'dart:convert';

class IPInfo {
  IPInfo(this.ip, this.ports, this.type);

  String ip = "";
  List<int> ports = [];

  /// main: 主  sec：副 video：视频
  String type = "";

  IPInfo.fromJson(Map<String, dynamic> data) {
    ip = data['ip'] as String;
    ports = [];
    data['ports'].forEach((dynamic e) => ports.add(int.parse('$e')));
    type = data['type'] as String;
  }

  Map<String, dynamic> toJson() => <String, dynamic>{
        'ip': ip,
        'ports': ports,
        'type': type,
      };

  @override
  String toString() {
    return 'IPInfo{ip: $ip, ports: $ports, type: $type}';
  }
}
