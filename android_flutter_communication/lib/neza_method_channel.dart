import 'dart:collection';

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

  sayHelloToNativeWithParam() async {
    var map = HashMap();
    map['name'] = '江澎涌';
    map['age'] = 28;
    var result = await _channel?.invokeMethod("sayHelloToNativeWithParam", map);
    print('sayHelloToNativeWithParam result: $result');
  }

  sayHelloToNativeWithRaw() async {
    var map = HashMap();
    map['name'] = 'Jiang Peng Yong';
    map['age'] = 27;
    var result = await _channel?.invokeMethod("sayHelloToNativeWithRaw", map);
    print('sayHelloToNativeWithParam result: $result');
  }

  Future<dynamic> methodCallHandler(MethodCall call) async {
    var method = call.method;
    var arguments = call.arguments;
    switch (method) {
      case "sayHelloToFlutter":
        print("=========== [Native to Flutter] sayHelloToFlutter ===========");
        break;
      case "sayHelloToFlutterWithCallback":
        print(
            "=========== [Native to Flutter] sayHelloToFlutterWithCallback ===========");
        if (arguments is HashMap) {
          var name = arguments["name"];
          var age = arguments["age"];
          print("sayHelloToFlutterWithCallback[name: $name, age: $age]");
        }
        return "Flutter had received.";
    }
  }
}
