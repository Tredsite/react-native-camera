package com.baebae.reactnativecamera.cameralib.v2.utils;

import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.baebae.reactnativecamera.cameralib.v1.CameraV1Container;
import com.baebae.reactnativecamera.cameralib.v1.utils.CameraHandlerThread;
import com.baebae.reactnativecamera.cameralib.v2.CameraV2Container;

// This code is mostly based on the top answer here: http://stackoverflow.com/questions/18149964/best-use-of-handlerthread-over-other-similar-classes
public class CameraV2HandlerThread extends HandlerThread {
    private static final String LOG_TAG = "CameraHandlerThread";

    private CameraV2Container v2Container;

    public CameraV2HandlerThread(CameraV2Container scannerView) {
        super("CameraHandlerThread");
        v2Container = scannerView;
        start();
    }

    public void startCamera() {
        Handler localHandler = new Handler(getLooper());
        localHandler.post(new Runnable() {
            @Override
            public void run() {
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        v2Container.setupCameraPreview();
                    }
                });
            }
        });
    }

}
