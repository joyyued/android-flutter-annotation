import 'package:flutter/services.dart';

const String NEZA_METHOD_CHANNEL =
    'com.zinc.android_flutter_annotation/nezaMethodChannel';

class NezaMethodChannel {
  /// 单例
  static NezaMethodChannel? _instance;

  NezaMethodChannel._internal();

  factory NezaMethodChannel() => _getInstance();

  static NezaMethodChannel get instance => _getInstance();

  static _getInstance() {
    if (_instance == null) _instance = NezaMethodChannel._internal();
    return _instance;
  }

  MethodChannel? _channel;

  void init() {
    _channel = MethodChannel(NEZA_METHOD_CHANNEL)
      ..setMethodCallHandler(methodCallHandler);
  }

  sayHelloToNative() {
    _channel?.invokeMethod("sayHelloToNative");
  }

  Future<dynamic> methodCallHandler(MethodCall call) async {
    var method = call.method;
    var arguments = call.arguments;
    if (method == "sayHelloToFlutter") {
      print("=========== [Native to Flutter] sayHelloToFlutter ===========");
    }
  }
}
