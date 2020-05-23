import 'dart:async';

import 'package:flutter/services.dart';

class IdVerifyPlugin {
  static const MethodChannel _channel = const MethodChannel('com.hs.flutter.idVerify/method_channel');

  static Future<String> get deviceSn async {
    final String version = await _channel.invokeMethod('getDeviceSN');
    return version;
  }

  /// sdk激活
  static Future<bool> activeSdk(String appId, String sdkKey) async {
    try {
      await _channel.invokeMethod('activeSdk', {
        'appId': appId,
        'sdkKey': sdkKey
      });
      return true;
    } catch (_) {
      return false;
    }
  }
}
