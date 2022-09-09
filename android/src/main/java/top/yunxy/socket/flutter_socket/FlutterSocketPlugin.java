package top.yunxy.socket.flutter_socket;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import top.yunxy.socket.flutter_socket.core.IPInfo;
import top.yunxy.socket.flutter_socket.core.SocketIOManage;

/**
 * FlutterSocketPlugin
 */
public class FlutterSocketPlugin implements FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;

    private static EventChannel eventChannel;

    private static EventChannel.EventSink eventSink;

    private SocketIOManage socketIOManage = null;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_socket");
        eventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_socket_event");
        channel.setMethodCallHandler(this);
        eventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object arguments, EventChannel.EventSink events) {
                System.out.println("eventSink success");
                eventSink = events;
            }

            @Override
            public void onCancel(Object arguments) {
                eventSink = null;
            }
        });
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        Gson gson = new Gson();
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android" + android.os.Build.VERSION.RELEASE);
        } else if (call.method.equals("connect")) {
            List<String> codes = call.argument("codes");
            JsonArray jsonArray = gson.fromJson((String) call.argument("ipInfoList"), JsonArray.class);
            List<IPInfo> ipInfoList = new ArrayList<>();
            for (JsonElement e : jsonArray) {
                IPInfo ipInfo = gson.fromJson(e, IPInfo.class);
                ipInfoList.add(ipInfo);
            }
            boolean video = call.argument("video");
            boolean heartbeat = call.argument("heartbeat");
            connect(codes, ipInfoList, video, heartbeat);
            result.success(null);
        } else if (call.method.equals("close")) {
            close();
            result.success(null);
        } else if (call.method.equals("send")) {
            String code = call.argument("code");
            String socketType = call.argument("socketType");
            int msgId = call.argument("msgId");
            String data = call.argument("data");
            send(code, socketType, msgId, data);
            result.success(null);
        } else if (call.method.equals("sendMedia")) {
            String code = call.argument("code");
            String socketType = call.argument("socketType");
            String t0800 = call.argument("t0800");
            String t0200 = call.argument("t0200");
            byte[] data = call.argument("data");
            sendMedia(code, socketType, t0800, t0200, data);
            result.success(null);
        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        eventChannel.setStreamHandler(null);
        socketIOManage = null;
    }

    public synchronized void connect(List<String> codes, List<IPInfo> ipInfoList, boolean video, boolean heartbeat) {
        if (socketIOManage != null) {
            socketIOManage.close();
            socketIOManage = null;
        }
        socketIOManage = new SocketIOManage(codes, ipInfoList, video, heartbeat, data -> {
            sendFlutter(data);
        });
    }

    public synchronized void close() {
        if (socketIOManage == null) {
            return;
        }
        socketIOManage.close();
    }

    public synchronized void send(String code, String socketType, int msgId, String data) {
        if (socketIOManage == null) {
            return;
        }
        socketIOManage.send(code, socketType, msgId, data);
    }

    public synchronized void sendMedia(String code, String socketType, String t0800, String t0200, byte[] data) {
        if (socketIOManage == null) {
            return;
        }
        socketIOManage.sendMedia(code, socketType, t0800, t0200, data);
    }

    public synchronized void sendFlutter(Object o) {
        if (eventSink != null) {
            eventSink.success(o);
        }
    }
}
