package com.baebae.reactnativecamera.cameralib;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.baebae.reactnativecamera.barcode.Scan;
import com.baebae.reactnativecamera.cameralib.helpers.CameraHelpers;
import com.baebae.reactnativecamera.cameralib.v1.CameraV1Container;
import com.baebae.reactnativecamera.cameralib.v1.CameraView;
import com.baebae.reactnativecamera.cameralib.v1.utils.CameraHandlerThread;
import com.baebae.reactnativecamera.cameralib.v1.utils.CameraUtils;
import com.baebae.reactnativecamera.cameralib.v1.utils.CameraInstanceManager;
import com.google.zxing.Result;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

public class CameraPreviewLayout extends FrameLayout implements CameraCallback{

    private FrameLayout cameraLayout = null;
    private View cameraPreview;
    private static boolean flagPreviewInitialized = false;
    private CameraV1Container v1Container = null;

    public CameraPreviewLayout(Context context, CameraInstanceManager cameraInstanceManager, Activity appActivity) {
        super(context);
        v1Container = new CameraV1Container(cameraInstanceManager, this, appActivity);
    }

    protected  void changeCameraOrientation(final int orientation, final Runnable callback) {
        CameraView.changeOrientation(orientation);
    }

    private void setupLayout(View view) {
        cameraPreview = view;
        if (cameraLayout == null) {
            removeView(cameraLayout);
        }

        cameraLayout = new FrameLayout(getContext());
        cameraLayout.setBackgroundColor(Color.BLACK);
        cameraLayout.addView(cameraPreview, 0);

        addView(cameraLayout, 0);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                moveToBack(cameraLayout);
            }
        }, 300);
    }

    private void moveToBack(View currentView) {
        if (currentView != null) {
            ViewGroup viewGroup = ((ViewGroup) currentView.getParent());
            int index = viewGroup.indexOfChild(currentView);
            for (int i = 0; i < index; i++) {
                View v = viewGroup.getChildAt(i);
                v.bringToFront();
                Log.d("Test", "Move to back" + v);
                viewGroup.bringChildToFront(v);
            }
            viewGroup.invalidate();
            ((View)currentView.getParent()).requestLayout();
        }
    }

    protected void unregisterLifecycleEventListener() {
    }

    protected void registerLifecycleEventListener() {
    }

    public void startCamera() {
        v1Container.startCamera();
    }

    public boolean isRunning() {
        return flagPreviewInitialized;
    }

    public void stopCamera() {
        if (!flagPreviewInitialized) {
            return;
        }
        unregisterLifecycleEventListener();

        if (cameraLayout != null) {
            cameraLayout.removeView(cameraPreview);
            removeView(cameraLayout);
            cameraLayout = null;
        }

        flagPreviewInitialized = false;
        v1Container.stopCamera();
    }

    public void setFlash(boolean flagTorch) {
        v1Container.toggleTorch(flagTorch);
    }


    protected void onImageFileSaved(String imagePath) {
    }

    protected void onBarcodeScanned(Result str) {
    }


    public void takePicture(boolean flagMode) {
        v1Container.takePicture(flagMode);
    }

    @Override
    public void onResultViewInitialized(View view) {
        setupLayout(view);
    }

    @Override
    public void onResultImageCaptured(String fileName) {
        onImageFileSaved(fileName);
    }

    @Override
    public void onResultBarcodeScanned(Result str) {
        onBarcodeScanned(str);
    }

    @Override
    public void onResultCameraInitialized() {
        flagPreviewInitialized = true;
        registerLifecycleEventListener();
    }
}
