# flutter_id_verify 这是一个人证检测的Flutter插件，通过提交身份信息byte与人脸信息比对检测身份与人脸是否比对成功

## 使用方法

### 使用注意

开启权限
```
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.CAMERA"/>
```

### 加载依赖
```
idverify:
    git:
        https://github.com/hisan-web/flutter_id_verify.git
```

### 使用
```
    /// 注意minisdk为21

    /// 注册sdk，第一次安装或者重新安装后需要注册sdk
    IdVerifyPlugin.activeSdk(appId, sdkKey);

    /// 获取设备sn号
    String sn = IdVerifyPlugin.deviceSn;
```

### 使用demo

```
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
```

