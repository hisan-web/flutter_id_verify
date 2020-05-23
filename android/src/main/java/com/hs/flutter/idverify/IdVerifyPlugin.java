package com.hs.flutter.idverify;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.arcsoft.idcardveri.IdCardVerifyError;
import com.arcsoft.idcardveri.IdCardVerifyManager;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** IdverifyPlugin */
public class IdVerifyPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {

  private static final String TAG = "IdVerifyPlugin";
  private static final String METHOD_CHANNEL = "com.hs.flutter.idVerify/method_channel";
  private static final String VIEW_CHANNEL = "com.hs.flutter.idVerify/view_channel";

  private FlutterPluginBinding flutterPluginBinding;
  private Activity activity;

  public static void registerWith(Registrar registrar) {
    final IdVerifyPlugin instance = new IdVerifyPlugin();
    instance.pluginRegister(registrar.activity(), registrar.messenger());
    // If the sdk is less than 21 (min sdk for Camera2) we don't register the plugin.
    if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
      return;
    }
    registrar.platformViewRegistry().
            registerViewFactory(VIEW_CHANNEL, new IdVerifyFactory(
                    registrar.activity(),
                    registrar.messenger()
            ));
  }

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
    flutterPluginBinding = binding;
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    pluginDestroy();
  }

  @Override
  public void onAttachedToActivity(ActivityPluginBinding binding) {
    pluginRegister(binding.getActivity(), flutterPluginBinding.getBinaryMessenger());
    // If the sdk is less than 21 (min sdk for Camera2) we don't register the plugin.
    if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
      return;
    }
    flutterPluginBinding.getPlatformViewRegistry()
            .registerViewFactory(VIEW_CHANNEL, new IdVerifyFactory(
                    binding.getActivity(),
                    flutterPluginBinding.getBinaryMessenger()
            ));
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    onDetachedFromActivity();
  }

  @Override
  public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
    onAttachedToActivity(binding);
  }

  @Override
  public void onDetachedFromActivity() {
    pluginDestroy();
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    switch (call.method) {
      case "getDeviceSN":
        result.success(getDeviceSn());
        break;
      case "activeSdk":
        String appId = call.argument("appId");
        String sdkKey = call.argument("sdkKey");
        activeSdk(appId, sdkKey, result);
        break;
      default:
        result.notImplemented();
    }
  }

  @SuppressLint("HardwareIds")
  private String getDeviceSn() {
    return android.os.Build.SERIAL;
  }

  /**
   * 激活SDK
   */
  private void activeSdk(String appId, String sdkKey, Result result) {
    Log.i(TAG,"激活sdk，Activity："+activity+"，appId："+appId+"sdkKey："+sdkKey);
    int ret = IdCardVerifyManager.getInstance().active(activity,appId,sdkKey);
    if (ret != IdCardVerifyError.OK && ret != IdCardVerifyError.MERR_ASF_ALREADY_ACTIVATED) {
      Log.e(TAG,"sdk激活失败，失败错误码："+ret);
      result.error(""+ret, "sdk激活失败，请根据错误码查询错误问题", null);
      return;
    }
    Log.i(TAG,"sdk激活成功");
    result.success("sdk激活成功");
  }

  // 插件注册方法
  private void pluginRegister(Activity activity, BinaryMessenger messenger) {
    this.activity = activity;
    Log.i(TAG,"插件注册，Activity："+activity);
    final MethodChannel methodChannel = new MethodChannel(messenger, METHOD_CHANNEL);
    methodChannel.setMethodCallHandler(this);
  }

  // 插件销毁
  private void pluginDestroy() {
    flutterPluginBinding = null;
  }
}
