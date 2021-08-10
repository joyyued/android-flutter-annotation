import 'package:flutter/services.dart';

const String NEZA_BASIC_CHANNEL =
    'com.zinc.android_flutter_annotation/nezaBasicChannel';

class NezaBasicChannel {
  /// 单例
  static NezaBasicChannel? _instance;

  NezaBasicChannel._internal();

  factory NezaBasicChannel() => _getInstance();

  static NezaBasicChannel get instance => _getInstance();

  static _getInstance() {
    if (_instance == null) _instance = NezaBasicChannel._internal();
    return _instance;
  }

  BasicMessageChannel? _channel;

  void init() {
    _channel = BasicMessageChannel(NEZA_BASIC_CHANNEL, StringCodec())
      ..setMessageHandler(methodCallHandler);
  }

  sendJsonToNative() {
    _channel?.send('{"name":"江澎涌", "weight": "60kg"}');
  }

  Future<dynamic> methodCallHandler(dynamic message) async {
    if (message is String) {
      print("=========== [Native to Flutter] $message ===========");
    }
  }
}
