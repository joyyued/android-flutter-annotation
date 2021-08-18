import 'dart:collection';
import 'dart:ui' as ui;
import 'dart:async';
import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

typedef ImageCallback = Function(Uint8List);

const String NATIVE_IMAGE_CHANNEL_NAME =
    'com.zinc.android_flutter_annotation/nezaEventChannel';

class NezaEventChannel {
  /// 单例
  static NezaEventChannel? _instance;

  NezaEventChannel._internal();

  factory NezaEventChannel() => _getInstance();

  static NezaEventChannel get instance => _getInstance();

  static _getInstance() {
    if (_instance == null) _instance = NezaEventChannel._internal();
    return _instance;
  }

  ImageCallback? imageCallback;

  init() {
    EventChannel(NATIVE_IMAGE_CHANNEL_NAME).receiveBroadcastStream().listen(
      (receiveData) async {
        // 图片 byte
        // final image = await loadImageFromList(receiveData);

        var map = receiveData as Map;
        if (map.containsKey('id')) {
          var id = map['id'];
          print('id: $id');
        }

        final _imageCallback = imageCallback;
        if (_imageCallback != null) {
          _imageCallback(map["byteArray"]);
        }
      },
    );
  }
}

loadImageFromList(
  Uint8List imageData,
) async {
  ImageStream stream;
  stream = Image.memory(imageData).image.resolve(
        ImageConfiguration.empty,
      );

  Completer<ui.Image> completer = Completer<ui.Image>();
  void listener(ImageInfo frame, bool synchronousCall) {
    final ui.Image image = frame.image;
    completer.complete(image);
    stream.removeListener(ImageStreamListener(listener));
  }

  stream.addListener(ImageStreamListener(listener));
  return completer.future;
}
