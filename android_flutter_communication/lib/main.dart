import 'dart:typed_data';

import 'package:android_flutter_communication/neza_customer_basic_channel.dart';
import 'package:android_flutter_communication/neza_standard_basic_channel.dart';
import 'package:android_flutter_communication/neza_string_basic_channel.dart';
import 'package:android_flutter_communication/neza_event_channel.dart';
import 'package:android_flutter_communication/neza_method_channel.dart';
import 'package:flutter/material.dart';

void main() {
  runApp(MyApp());
  NezaMethodChannel.instance.init();
  NezaEventChannel.instance.init();
  NezaStringBasicChannel.instance.init();
  NezaStandardBasicChannel.instance.init();
  NezaCustomerBasicChannel.instance.init();
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key? key, required this.title}) : super(key: key);

  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  Uint8List? _image = null;

  @override
  void initState() {
    NezaEventChannel.instance.imageCallback = (image) {
      setState(() {
        _image = image;
      });
    };
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          children: getChildren(),
        ),
      ),
    );
  }

  List<Widget> getChildren() {
    List<Widget> result = <Widget>[];
    if (_image != null) {
      result.add(Image.memory(_image!));
    }
    result.add(
      TextButton(
        onPressed: () {
          NezaMethodChannel.instance.sayHelloToNative();
        },
        child: Text('Say hello to native'),
      ),
    );
    result.add(
      TextButton(
        onPressed: () {
          NezaMethodChannel.instance.sayHelloToNativeWithParam();
        },
        child: Text('Say hello to native [callback success]'),
      ),
    );
    result.add(
      TextButton(
        onPressed: () {
          NezaMethodChannel.instance.sayHelloToNativeWithRaw();
        },
        child: Text('Say hello to native [callback error]'),
      ),
    );
    result.add(
      TextButton(
        onPressed: () {
          NezaMethodChannel.instance.methodNewName();
        },
        child: Text('Say hello to native [method]'),
      ),
    );
    result.add(
      TextButton(
        onPressed: () {
          NezaStringBasicChannel.instance.sendJsonToNative();
        },
        child: Text('Send json to native'),
      ),
    );
    result.add(
      TextButton(
        onPressed: () {
          NezaStandardBasicChannel.instance.sendToNative();
        },
        child: Text('Send map to native'),
      ),
    );
    result.add(
      TextButton(
        onPressed: () {
          NezaCustomerBasicChannel.instance.sendToNative();
        },
        child: Text('Send map to native [ Customer codec ] '),
      ),
    );
    return result;
  }
}
