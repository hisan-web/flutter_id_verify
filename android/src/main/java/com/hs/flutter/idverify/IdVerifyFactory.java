package com.hs.flutter.idverify;

import android.app.Activity;
import android.content.Context;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;

public class IdVerifyFactory extends PlatformViewFactory {

    private final BinaryMessenger binaryMessenger;
    private final Activity activity;

    IdVerifyFactory(Activity activity, BinaryMessenger binaryMessenger) {
        super(StandardMessageCodec.INSTANCE);
        this.binaryMessenger = binaryMessenger;
        this.activity = activity;
    }

    @Override
    public PlatformView create(Context context, int viewId, Object args) {
        return new IdVerifyView(activity, binaryMessenger, viewId, args);
    }
}
