package com.baebae.reactnativecamera.cameralib;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.baebae.reactnativecamera.barcode.Scan;
import com.baebae.reactnativecamera.cameralib.v1.CameraV1Container;
import com.baebae.reactnativecamera.cameralib.v1.CameraView;
import com.baebae.reactnativecamera.cameralib.v1.utils.CameraInstanceManager;
import com.baebae.reactnativecamera.cameralib.v2.CameraV2Container;
import com.baebae.reactnativecamera.cameralib.v2.CameraV2Preview;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;


public class CameraPreviewLayout extends FrameLayout implements CameraCallback{

    private FrameLayout cameraLayout = null;
    private View cameraPreview;
    private static boolean flagPreviewInitialized = false;
    private Boolean flagUsingV2Api = true;

    private CameraV1Container v1Container = null;
    private CameraV2Container v2Container = null;

    private Scan barcodeScanner = null;

    public CameraPreviewLayout(Context context, CameraInstanceManager cameraInstanceManager, Activity appActivity) {
        super(context);
        barcodeScanner = new Scan(appActivity);

        if (Build.VERSION.SDK_INT >= 21) {
            flagUsingV2Api = true;
        } else {
            flagUsingV2Api = false;
        }
        
        if (flagUsingV2Api) {
            v2Container = new CameraV2Container(this, appActivity, barcodeScanner);
        } else {
            v1Container = new CameraV1Container(cameraInstanceManager, this, appActivity, barcodeScanner);
        }

    }

    protected  void changeCameraOrientation(final int orientation, final Runnable callback) {
        CameraView.changeOrientation(orientation);
        CameraV2Preview.changeOrientation(orientation);
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
        if (flagUsingV2Api) {
            v2Container.startCamera();;
        } else {
            v1Container.startCamera();
        }
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
        if (flagUsingV2Api) {
            v2Container.stopCamera();

        } else {
            v1Container.stopCamera();
        }
    }

    public void setFlash(boolean flagTorch) {
        if (flagUsingV2Api) {
            v2Container.toggleTorch(flagTorch);
        } else {
            v1Container.toggleTorch(flagTorch);
        }
    }


    protected void onImageFileSaved(String imagePath) {
    }

    protected void onBarcodeScanned(Result str) {
    }

    public void takePicture(boolean flagMode) {
        if (flagUsingV2Api) {
            v2Container.takePicture(flagMode);
        } else {
            v1Container.takePicture(flagMode);
        }
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
    public void onResultCameraInitialized() {
        flagPreviewInitialized = true;
        registerLifecycleEventListener();
    }

    @Override
    public void onPreviewImage(byte[] data, int width, int height) {
        barcodeScanner.scanImage(data, width, height, new Scan.ResultCallback() {
            @Override
            public void onDecodeBarcode(Result result) {
                onBarcodeScanned(result);
            }
        });
    }
}
