import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:idverify/id_verify_plugin.dart';

void main() {
  const MethodChannel channel = MethodChannel('idverify');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await IdVerifyPlugin.platformVersion, '42');
  });
}
