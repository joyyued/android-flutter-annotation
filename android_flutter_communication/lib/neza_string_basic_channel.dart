import 'package:flutter/services.dart';

const String NEZA_String_BASIC_CHANNEL =
    'com.zinc.android_flutter_annotation/nezaStringBasicChannel';

class NezaStringBasicChannel {
  /// 单例
  static NezaStringBasicChannel? _instance;

  NezaStringBasicChannel._internal();

  factory NezaStringBasicChannel() => _getInstance();

  static NezaStringBasicChannel get instance => _getInstance();

  static _getInstance() {
    if (_instance == null) _instance = NezaStringBasicChannel._internal();
    return _instance;
  }

  BasicMessageChannel? _channel;

  void init() {
    _channel = BasicMessageChannel(
      NEZA_String_BASIC_CHANNEL,
      StringCodec(),
    )..setMessageHandler(methodCallHandler);
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
