import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_socket/flutter_socket.dart';

import 'ip_info.dart';
import 'dart:convert';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
    FlutterSocket.stream.listen((event) {
      Map<String, dynamic> data = json.decode(event) as Map<String, dynamic>;
      switch(data['type']) {
        case 'connect':
          final bool state = data['content'] as bool;
          print('connect: $state');
          break;
        case 'currentIndex':
          final int currentIndex = data['content'] as int;
          print('currentIndex: $currentIndex');
          break;
        case 'auth':
          break;
        case 'event':
          break;
        default:
          String log = data['content'] as String;
          print(log);
          break;
      }
    });
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion =
          await FlutterSocket.platformVersion ?? 'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: [
              Text('Running on: $_platformVersion\n'),
              RaisedButton(onPressed: () {
                // final List<String> codes = ['13120210113', '14120203335'];
                String licensePlate = '???AS6257';
                List<String> codes = ['13120210113'];
                final List<IPInfo> ipInfoList = [
                  IPInfo('112.051.008.008', [7611, 7612, 7613], 'main'),
                  // IPInfo('39.101.130.136', [8085], 'sec'),
                  // IPInfo('61.154.39.40', [50191], 'video')
                ];

                FlutterSocket.connect(codes, json.encode(ipInfoList), true, true);
              }, child: const Text('??????'),),
              RaisedButton(onPressed: () {
                FlutterSocket.close();
              }, child: const Text('??????'),),
              RaisedButton(onPressed: () {
                String data = '{"msgId":512,"alarm":0,"state":3,"latitude":26.11698,"longitude":119.23654,"altitude":0,"speed":10.9,"direction":45,"time":"2022-09-09 13:04:29","mileage":99739.67}';
                FlutterSocket.send(0x0200, data);
              }, child: const Text('T0200'),),
              RaisedButton(onPressed: () {
                String data = '{"msgId":3841,"longitude":119.23654,"latitude":26.11698,"altitude":0,"speed":10.9,"direction":45,"time":"2022-09-09 13:04:29","licensePlate":"???AS6257","driverId":"13120210113","carriageState":"??????","liftState":"??????","emptyWeightState":"??????"}';
                FlutterSocket.send(0x0f01, data);
              }, child: const Text('T0F01'),),
            ],
          ),
        ),
      ),
    );
  }
}
