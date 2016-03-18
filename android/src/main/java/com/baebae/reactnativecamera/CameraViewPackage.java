package com.baebae.reactnativecamera;

import android.app.Activity;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import com.baebae.reactnativecamera.cameralib.helpers.CameraInstanceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CameraViewPackage implements ReactPackage {
    private CameraViewManager cameraViewManager = null;
    private CameraInstanceManager cameraInstanceManager;
    private Activity appActivity = null;
    public CameraViewPackage(Activity appActivity) {
        this.appActivity = appActivity;
        this.cameraInstanceManager = new CameraInstanceManager();
    }

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactApplicationContext) {
        return Arrays.<NativeModule>asList(
                new LockOrientation(reactApplicationContext, appActivity)
        );
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactApplicationContext) {
        cameraViewManager = new CameraViewManager(reactApplicationContext, cameraInstanceManager, appActivity);
        return Arrays.<ViewManager>asList(
                cameraViewManager
        );
    }

    @Override
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Arrays.asList();
    }

}