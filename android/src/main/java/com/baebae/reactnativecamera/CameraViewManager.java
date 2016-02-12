package com.baebae.reactnativecamera;

import com.baebae.reactnativecamera.cameralib.helpers.CameraInstanceManager;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.view.View;

import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewGroupManager;

import java.util.Map;

public class CameraViewManager extends ViewGroupManager<CameraView> {
    public static final String REACT_CLASS = "CameraViewAndroid";
    private ReactApplicationContext reactApplicationContext = null;
    private CameraView cameraView = null;
    private CameraInstanceManager cameraInstanceManager;

    private Activity appActivity = null;
    public CameraViewManager(ReactApplicationContext reactApplicationContext, CameraInstanceManager cameraInstanceManager, Activity appActivity) {
        this.reactApplicationContext = reactApplicationContext;
        this.cameraInstanceManager = cameraInstanceManager;
        this.appActivity = appActivity;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected CameraView createViewInstance(ThemedReactContext context) {
        cameraView = new CameraView(context, cameraInstanceManager, appActivity);
        return cameraView;
    }

    @ReactProp(name = "startCamera")
    public void startCamera(CameraView view, @Nullable Boolean flagValue) {
        if (flagValue) {
            view.startCamera();
            view.registerLifecycleEventListener();
        } else if (flagValue) {
            view.stopCamera();
            view.unregisterLifecycleEventListener();
        }
    }

    @ReactProp(name = "startCapture")
    public void startCapture(CameraView view, @Nullable String flagValue) {
        if (flagValue != null && flagValue.equals("portrait")) {
            view.takePicture(true);
        } else if (flagValue != null && flagValue.equals("landscape")) {
            view.takePicture(false);
        }
    }

    @ReactProp(name = "torchMode")
    public void toggleTorch(CameraView view, @Nullable Boolean flagValue) {
        if (flagValue) {
            view.setFlash(true);
        } else {
            view.setFlash(false);
        }
    }

    @Override
    public void addView(CameraView parent, View child, int index) {
        parent.addView(child, index);
    }
}