import 'dart:typed_data';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

typedef void IdVerifyViewCreatedCallback(IdVerifyViewController controller);

class IdVerifyView extends StatefulWidget {

  final IdVerifyViewCreatedCallback idVerifyViewCreatedCallback;

  IdVerifyView({
    Key key,
    @required this.idVerifyViewCreatedCallback,
  });

  @override
  _IdVerifyViewState createState() => _IdVerifyViewState();
}

class _IdVerifyViewState extends State<IdVerifyView> {
  @override
  Widget build(BuildContext context) {
    if (defaultTargetPlatform == TargetPlatform.android) {
      return AndroidView(
        viewType: 'com.hs.flutter.idVerify/view_channel',
        onPlatformViewCreated: onPlatformViewCreated,
        creationParamsCodec: const StandardMessageCodec(),
      );
    } else if (defaultTargetPlatform == TargetPlatform.iOS) {
      return UiKitView(
        viewType: 'com.hs.flutter.idVerify/view_channel',
        onPlatformViewCreated: onPlatformViewCreated,
        creationParamsCodec: const StandardMessageCodec(),
      );
    }
    return Text("$defaultTargetPlatform not support");
  }

  /// 视图创建回调，目的是创建完成视图后才进行channel初始化
  Future<void> onPlatformViewCreated(id) async {
    if (widget.idVerifyViewCreatedCallback == null) {
      return;
    }
    widget.idVerifyViewCreatedCallback(IdVerifyViewController.init(id));
  }
}

class IdVerifyViewController {
  MethodChannel _methodChannel;
  EventChannel _eventChannel;

  /// 人脸数据流监听
  void idVerifyViewDataStreamListen(dynamic success, {dynamic error}) {
    _eventChannel.receiveBroadcastStream().listen((data) {
      success(data);
    }, onError: error);
  }

  /// 引擎初始化
  Future<void> initEngine() async {
    _methodChannel.invokeMethod("initEngine");
  }

  /// 销毁引擎
  Future<void> unInitEngine() async {
    _methodChannel.invokeMethod("unInitEngine");
  }

  /// 输入身份信息
  Future<void> inputIdCard(Uint8List bytes) async {
    _methodChannel.invokeMethod("inputIdCard", {'bytes': bytes});
  }

  IdVerifyViewController.init(int id) {
    _methodChannel = MethodChannel("com.hs.flutter.idVerify/view_method_$id");
    _eventChannel = EventChannel("com.hs.flutter.idVerify/view_event_$id");
  }
}
