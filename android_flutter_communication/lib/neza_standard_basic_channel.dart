import 'dart:collection';

import 'package:flutter/services.dart';

const String NEZA_STANDARD_BASIC_CHANNEL =
    'com.zinc.android_flutter_annotation/nezaStandardBasicChannel';

class NezaStandardBasicChannel {
  /// 单例
  static NezaStandardBasicChannel? _instance;

  NezaStandardBasicChannel._internal();

  factory NezaStandardBasicChannel() => _getInstance();

  static NezaStandardBasicChannel get instance => _getInstance();

  static _getInstance() {
    if (_instance == null) _instance = NezaStandardBasicChannel._internal();
    return _instance;
  }

  BasicMessageChannel? _channel;

  void init() {
    _channel = BasicMessageChannel(
      NEZA_STANDARD_BASIC_CHANNEL,
      StandardMessageCodec(),
    )..setMessageHandler(methodCallHandler);
  }

  sendToNative() {
    var map = HashMap<String, dynamic>();
    map["name"] = "江澎涌";
    map["weight"] = 62;
    _channel?.send(map);
  }

  Future<dynamic> methodCallHandler(dynamic message) async {
    var map = message as Map;
    var name = map["name"];
    var age = map["age"];
    print("=========== [Native to Flutter] Standart: $name $age ===========");
  }
}
