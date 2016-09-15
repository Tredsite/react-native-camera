package com.baebae.reactnativecamera;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Display;
import android.view.OrientationEventListener;

import com.baebae.reactnativecamera.cameralib.helpers.CameraInstanceManager;
import com.baebae.reactnativecamera.cameralib.ui.CameraPreviewLayout;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.google.zxing.Result;

public class CameraView extends CameraPreviewLayout implements LifecycleEventListener{

    private SensorManager sensorManager = null;
    private SensorEventListener orientationListener = null;
    private int mOrientation = -1;

    public CameraView(ThemedReactContext context, CameraInstanceManager cameraInstanceManager) {
        super(context, cameraInstanceManager);
    }

    public static final int ORIENTATION_UNKNOWN = -1;
    private static final int ORIENTATION_KEEP_THRESHOLD = 3;
    private static final int ORIENTATION_TIME_THRESHOLD = 500;
    private static int orientationKeepCount = 0;
    private static long lastOrientationTime = 0;
    private static int lastOrientationValue = ORIENTATION_UNKNOWN;
    private boolean flagOrientationChanged = false;

    private static int getGeneralOrientation(int degrees){
        if(degrees >= 330 || degrees <= 30 ) return 90;
        if(degrees <= 300 && degrees >= 240) return 0;
        if(degrees <= 210 && degrees >= 160) return 90;
        if(degrees <= 120 && degrees >= 60) return 180;
        return -1;
    }

    private final Runnable mLayoutRunnable = new Runnable() {
        @Override
        public void run() {
            measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY));
            layout(getLeft(), getTop(), getRight(), getBottom());
        }
    };

    @Override
    protected void registerLifecycleEventListener() {
        super.registerLifecycleEventListener();
        ((ThemedReactContext)getContext()).addLifecycleEventListener(this);
    }

    @Override
    protected void unregisterLifecycleEventListener() {
        super.unregisterLifecycleEventListener();
        ((ThemedReactContext)getContext()).removeLifecycleEventListener(this);
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
        post(mLayoutRunnable);
    }

    @Override
    public void onHostResume() {
        startCamera();
    }

    @Override
    public void onHostPause() {
        stopCamera();
    }

    @Override
    public void onHostDestroy() {
        stopCamera();
    }

    @Override
    protected void onImageFileSaved(String imagePath) {
        super.onImageFileSaved(imagePath);
        WritableMap event = Arguments.createMap();

        event.putString("message", "file://" + imagePath);
        event.putString("type", "camera_capture");
        ReactContext reactContext = (ReactContext)getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                getId(),
                "topChange",
                event
        );

        stopCamera();
    }

    protected void onOrientationChanged(boolean portraitMode) {
        WritableMap event = Arguments.createMap();
        event.putBoolean("portraitMode", portraitMode);
        event.putString("type", "orientation_changed");
        ReactContext reactContext = (ReactContext)getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                getId(),
                "topChange",
                event
        );
    }

    @Override
    protected void onBarcodeScanned(Result barcodeResult) {
        super.onBarcodeScanned(barcodeResult);
        WritableMap event = Arguments.createMap();

        WritableMap message = Arguments.createMap();
        message.putString("data", barcodeResult.getText());
        message.putString("type", barcodeResult.getBarcodeFormat().name());

        WritableMap bounds = Arguments.createMap();
        WritableMap origin = Arguments.createMap();
        origin.putDouble("x", barcodeResult.getResultPoints()[0].getX());
        origin.putDouble("y", barcodeResult.getResultPoints()[0].getY());

        WritableMap size = Arguments.createMap();
        size.putDouble("height", Math.abs(barcodeResult.getResultPoints()[0].getY() - barcodeResult.getResultPoints()[1].getY()));
        size.putDouble("width", Math.abs(barcodeResult.getResultPoints()[0].getX() - barcodeResult.getResultPoints()[1].getX()));

        bounds.putMap("origin", origin);
        bounds.putMap("size", size);

        message.putMap("bounds", bounds);
        event.putMap("message", message);
        event.putString("type", "barcode_capture");
        ReactContext reactContext = (ReactContext)getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                getId(),
                "topChange",
                event
        );
    }
}
