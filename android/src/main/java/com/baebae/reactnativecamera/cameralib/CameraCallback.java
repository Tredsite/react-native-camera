package com.baebae.reactnativecamera.cameralib;

import android.view.View;

import com.google.zxing.Result;

/**
 * Created by baebae on 3/1/16.
 */
public interface CameraCallback {
    public abstract void onResultViewInitialized(View view);
    public abstract void onResultImageCaptured(String fileName);
    public abstract void onResultBarcodeScanned(Result str);
    public abstract void onResultCameraInitialized();
}
