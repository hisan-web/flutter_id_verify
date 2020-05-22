package com.hs.flutter.idverify;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;

import com.arcsoft.idcardveri.CompareResult;
import com.arcsoft.idcardveri.DetectFaceResult;
import com.arcsoft.idcardveri.IdCardVerifyError;
import com.arcsoft.idcardveri.IdCardVerifyListener;
import com.arcsoft.idcardveri.IdCardVerifyManager;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.hs.flutter.idverify.camera.CameraHelper;
import com.hs.flutter.idverify.camera.CameraListener;
import com.hs.flutter.idverify.impl.StreamHandlerImpl;
import com.hs.flutter.idverify.utils.BitmapUtil;
import com.hs.flutter.idverify.utils.DrawUtil;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.platform.PlatformView;

public class IdVerifyView implements PlatformView, MethodCallHandler, OnGlobalLayoutListener {

    private static final String TAG = "IdVerifyView";
    private static final String METHOD_CHANNEL_PREFIX = "com.hs.flutter.idVerify/view_method_";
    private static final String EVENT_CHANNEL_PREFIX = "com.hs.flutter.idVerify/view_event_";

    private Activity activity;
    private StreamHandlerImpl streamHandlerImpl;

    // view
    private View displayView;
    private SurfaceView surfacePrv;
    private SurfaceView surfaceRect;

    // init
    private boolean isInit = false;

    // camera
    private CameraHelper cameraHelper;
    private Camera.Size previewSize;
    private Integer rgbCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;

    //比对阈值，建议为0.82
    private static final double THRESHOLD = 0.82d;
    //视频或图片人脸数据是否检测到
    private boolean isCurrentReady = false;
    //身份证人脸数据是否检测到
    private boolean isIdCardReady = false;

    // 设置监听
    private IdCardVerifyListener idCardVerifyListener = new IdCardVerifyListener() {
        @Override
        public void onPreviewResult(DetectFaceResult detectFaceResult, byte[] bytes, int i, int i1) {
            Log.i(TAG, "获取到视频人脸数据："+detectFaceResult.getErrCode());
            if(detectFaceResult.getErrCode() == IdCardVerifyError.OK) {
                isCurrentReady = true;
                //需要在主线程进行比对
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        compare();
                    }
                });
            }
        }

        @Override
        public void onIdCardResult(DetectFaceResult detectFaceResult, byte[] bytes, int i, int i1) {
            Log.i(TAG, "获取到身份数据："+detectFaceResult.getErrCode());
            if(detectFaceResult.getErrCode() == IdCardVerifyError.OK) {
                isIdCardReady = true;
                //需要在主线程进行比对
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        compare();
                    }
                });
            }
        }
    };

    @SuppressLint("ObsoleteSdkInt")
    IdVerifyView(Activity activity, BinaryMessenger binaryMessenger, int viewId, Object args) {
        this.activity = activity;

        //
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams attributes = activity.getWindow().getAttributes();
            attributes.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            activity.getWindow().setAttributes(attributes);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        }

        // 建立通道
        MethodChannel methodChannel = new MethodChannel(binaryMessenger, METHOD_CHANNEL_PREFIX+viewId);
        methodChannel.setMethodCallHandler(this);
        // 建立Stream通道
        streamHandlerImpl = new StreamHandlerImpl(binaryMessenger, EVENT_CHANNEL_PREFIX+viewId);

        // 载入xml view层
        displayView = activity.getLayoutInflater().inflate(R.layout.activity_main,null);
        surfacePrv = displayView.findViewById(R.id.surfaceViewPrv);
        surfaceRect =  displayView.findViewById(R.id.surfaceViewRect);
        surfaceRect.setZOrderMediaOverlay(true);
        surfaceRect.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        // 添加view监听，在布局结束后才做初始化操作
        surfacePrv.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        surfacePrv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        initEngine();
        initCamera();
    }

    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
        switch (call.method) {
            case "initEngine":
                initEngine();
                result.success(isInit);
                break;
            case "unInitEngine":
                unInitEngine();
                result.success(isInit);
                break;
            case "inputIdCard":
                byte[] bytes = call.argument("bytes");
                inputIdCardData(bytes);
                result.success(bytes);
                break;
            default:
                result.notImplemented();
        }
    }

    @Override
    public View getView() {
        return displayView;
    }

    @Override
    public void dispose() {
        unInitEngine();
        if (cameraHelper!=null) {
            cameraHelper.stop();
            cameraHelper = null;
        }
    }

    private void initCamera() {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        // camera listener
        CameraListener cameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                Log.i(TAG, "onCameraOpened: " + cameraId + "  " + displayOrientation + " " + isMirror);
                previewSize = camera.getParameters().getPreviewSize();
            }

            @Override
            public void onPreview(byte[] data, Camera camera) {
                //
                if (isInit) {
                    DetectFaceResult result = IdCardVerifyManager.getInstance().onPreviewData(data, previewSize.width, previewSize.height, true);
                    if (result.getErrCode() != IdCardVerifyError.OK) {
                        Log.i(TAG, "onPreviewData video result: " + result.getErrCode());
                    }
                    if (surfaceRect != null) {
                        Canvas canvas = surfaceRect.getHolder().lockCanvas();
                        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                        Rect rect = result.getFaceRect();
                        if (rect != null) {
                            Rect adjustedRect = DrawUtil.adjustRect(rect, previewSize.width, previewSize.height,
                                    canvas.getWidth(), canvas.getHeight(), activity.getWindowManager().getDefaultDisplay().getRotation(), rgbCameraId);
                            //画人脸框
                            DrawUtil.drawFaceRect(canvas, adjustedRect, Color.YELLOW, 3);
                        }
                        surfaceRect.getHolder().unlockCanvasAndPost(canvas);
                    }
                }
            }

            @Override
            public void onCameraClosed() {
                Log.i(TAG, "onCameraClosed: ");
            }

            @Override
            public void onCameraError(Exception e) {
                Log.i(TAG, "onCameraError: " + e.getMessage());
            }

            @Override
            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
                Log.i(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
            }
        };

        cameraHelper = new CameraHelper.Builder()
                .previewViewSize(new Point(surfacePrv.getMeasuredWidth(), surfacePrv.getMeasuredHeight()))
                .rotation(activity.getWindowManager().getDefaultDisplay().getRotation())
                .specificCameraId(rgbCameraId != null ? rgbCameraId : Camera.CameraInfo.CAMERA_FACING_FRONT)
                .isMirror(false)
                .previewOn(surfacePrv)
                .cameraListener(cameraListener)
                .build();
        cameraHelper.init();
        cameraHelper.start();
    }

    /**
     * 初始化sdk
     */
    private void initEngine() {
        if (!isInit) {
            int ret = IdCardVerifyManager.getInstance().init(activity,idCardVerifyListener);
            if (ret != IdCardVerifyError.OK) {
                Log.e(TAG,"sdk初始化失败，失败错误码："+ret);
                return;
            }
            Log.i(TAG,"sdk初始化成功");
            isInit = true;
        }
    }

    /**
     * 销毁sdk
     */
    private void unInitEngine() {
        if (isInit) {
            IdCardVerifyManager.getInstance().unInit();
            isInit = false;
        }
    }

    private void inputIdCardData(byte[] bytes) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        Bitmap bmp = ArcSoftImageUtil.getAlignedBitmap(bitmap,true);
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        byte[] nv21 = BitmapUtil.bitmapToNv21(bmp, width, height);
        if(isInit) {
            DetectFaceResult result = IdCardVerifyManager.getInstance().inputIdCardData(nv21, width, height);
            Log.i(TAG, "inputIdCardData result: " + result.getErrCode());
        }
    }

    /**
     * 人证比对
     */
    private void compare() {
        if(!isCurrentReady || !isIdCardReady) {
            return;
        }
        //人证比对
        CompareResult compareResult = IdCardVerifyManager.getInstance().compareFeature(THRESHOLD);
        Log.i(TAG, "compareFeature: result " + compareResult.getResult() + ", isSuccess " + compareResult.isSuccess() + ", errCode " + compareResult.getErrCode());
        isIdCardReady = false;
        isCurrentReady = false;
        if (compareResult.getErrCode() == IdCardVerifyError.OK) {
            streamHandlerImpl.eventSinkSuccess(compareResult.isSuccess());
        }
    }
}
