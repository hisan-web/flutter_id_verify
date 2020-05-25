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

  static Future<String> aesEncrypt(String content, String key) async {
    final String str = await _channel.invokeMethod('aesEncrypt',{
      'content': content,
      'key': key
    });
    return str;
  }

  static Future<String> aesDecrypt(String content, String key) async {
    final String str = await _channel.invokeMethod('aesDecrypt',{
      'content': content,
      'key': key
    });
    return str;
  }
}
