import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:idverify/id_verify_plugin.dart';
import 'package:idverify/id_verify_view.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: HomePage(),
    );
  }
}

class HomePage extends StatefulWidget {
  @override
  _HomePageState createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {

  IdVerifyViewController controller;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
      ),
      body: IdVerifyView(
        idVerifyViewCreatedCallback: (controller) {
          this.controller = controller
              ..idVerifyViewDataStreamListen((data) {
                print(data);
              });
              // 输入身份信息的Uint8List
              //..inputIdCard(new Uint8List(0));
        },
      ),
    );
  }
}

