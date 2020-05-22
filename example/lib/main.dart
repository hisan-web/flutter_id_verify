import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'dart:async';

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

  Uint8List bytes;

  @override
  void initState() {
    // TODO: implement initState
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
      ),
      body: Column(children: <Widget>[
        Expanded(child: IdVerifyView(
          idVerifyViewCreatedCallback: (controller) {
            this.controller = controller
                ..idVerifyViewDataStreamListen((data) {
                  print("获取到人证回调数据：$data");
                });
//                ..inputIdCard(bytes);
          },
        ),),
        RaisedButton(
          onPressed: activeSdk,
          child: Text("点击激活SDK"),
        )
      ])
    );
  }

  Future<void> activeSdk() async {
    bool result = await IdVerifyPlugin.activeSdk("CjGWF7wY9uYDeKsUD8xcBdyM2Xkbc2AyjQkpc2gvFpWu", "HKyEKmpqQB84uyj7M23bQWiJokRB7JcwBVyYLp7TRSw9");
    print("激活SDK，返回结果：$result");
  }
}

