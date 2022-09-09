
import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/services.dart';

class FlutterSocket {
  static const MethodChannel _channel = MethodChannel('flutter_socket');
  static const EventChannel _eventChannel = EventChannel('flutter_socket_event');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Stream<dynamic> get stream {
    return _eventChannel.receiveBroadcastStream();
  }

  static Future<void> connect(List<String> codes, String ipInfoList, bool video, bool heartbeat) async {
    await _channel.invokeMethod('connect', {
      'codes': codes,
      'ipInfoList': ipInfoList,
      'video': video,
      'heartbeat': heartbeat
    });
    return Future.value();
  }

  static Future<void> close() async {
    await _channel.invokeMethod('close');
    return Future.value();
  }

  static Future<void> send(int msgId, String data, {String code = '', String socketType = ''}) async {
    await _channel.invokeMethod('send', {
      'code': code,
      'socketType': socketType,
      'msgId': msgId,
      'data': data
    });
    return Future.value();
  }

  static Future<void> sendMedia(String t0800, String t0200, Uint8List data, {String code = '', String socketType = ''}) async {
    await _channel.invokeMethod('sendMedia', {
      'code': code,
      'socketType': socketType,
      't0800': t0800,
      't0200': t0200,
      'data': data
    });
    return Future.value();
  }
}
