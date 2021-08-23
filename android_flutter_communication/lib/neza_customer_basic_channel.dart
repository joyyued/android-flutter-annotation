import 'dart:collection';
import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/services.dart';

const String NEZA_CUSTOMER_BASIC_CHANNEL =
    'com.zinc.android_flutter_annotation/nezaCustomerBasicChannel';

class NezaCustomerBasicChannel {
  /// 单例
  static NezaCustomerBasicChannel? _instance;

  NezaCustomerBasicChannel._internal();

  factory NezaCustomerBasicChannel() => _getInstance();

  static NezaCustomerBasicChannel get instance => _getInstance();

  static _getInstance() {
    if (_instance == null) _instance = NezaCustomerBasicChannel._internal();
    return _instance;
  }

  BasicMessageChannel? _channel;

  void init() {
    _channel = BasicMessageChannel(
      NEZA_CUSTOMER_BASIC_CHANNEL,
      HashMapCodec(),
    )..setMessageHandler(methodCallHandler);
  }

  sendToNative() {
    var map = HashMap<String, String>();
    map["name"] = "Jiang PengYong";
    map["weight"] = "60kg";
    _channel?.send(map);
  }

  Future<dynamic> methodCallHandler(dynamic message) async {
    var map = message as Map;
    print("=========== [Native to Flutter] Customer: $map ===========");
  }
}

const Utf8Codec utf8 = Utf8Codec();

class HashMapCodec implements MessageCodec<HashMap<String, String>?> {
  const HashMapCodec();

  @override
  HashMap<String, String>? decodeMessage(ByteData? message) {
    if (message == null) return null;
    String content = utf8.decoder.convert(message.buffer
        .asUint8List(message.offsetInBytes, message.lengthInBytes));

    HashMap<String, String> map = jsonDecode(content);
    return map;
  }

  @override
  ByteData? encodeMessage(HashMap<String, String>? map) {
    if (map == null) return null;
    final Uint8List encoded = utf8.encoder.convert(jsonEncode(map));
    return encoded.buffer.asByteData();
  }
}
