package com.baebae.reactnativecamera.cameralib.v2;

import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import com.baebae.reactnativecamera.barcode.Scan;
import com.baebae.reactnativecamera.cameralib.CameraCallback;
import com.baebae.reactnativecamera.cameralib.v2.utils.CameraV2HandlerThread;

/**
 * Created by baebae on 3/5/16.
 */
public class CameraV2Container {
    private CameraV2Preview mPreview;
    private View parentView;
    private Activity appActivity = null;
    private Scan barcodeScanner = null;
    private CameraCallback callback = null;
    private CameraV2HandlerThread mCameraHandlerThread = null;

    public CameraV2Container(View parentView, Activity appActivity, Scan barcodeScanner) {
        this.parentView = parentView;
        this.appActivity = appActivity;
        this.barcodeScanner = barcodeScanner;
        callback = (CameraCallback)parentView;
    }


    public void startCamera() {
        if(mCameraHandlerThread == null) {
            mCameraHandlerThread = new CameraV2HandlerThread(this);
        }
        mCameraHandlerThread.startCamera();
    }

    public void setupCameraPreview() {
        setupLayout();
        parentView.postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.onResultCameraInitialized();
            }
        }, 1000);
//        setAutoFocus(mAutoFocusState);
    }

    public void stopCamera() {
        mPreview.stopCamera();
        if(mCameraHandlerThread != null) {
            mCameraHandlerThread.quit();
            mCameraHandlerThread = null;
        }
    }

    public void toggleTorch(boolean flagTorch) {
//        mFlashState = flagTorch;
//        if(mCamera != null && CameraUtils.isFlashSupported(mCamera)) {
//            Camera.Parameters parameters = mCamera.getParameters();
//            if(!flagTorch) {
//                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
//            } else {
//                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
//            }
//            mCamera.setParameters(parameters);
//        }
    }

    public void takePicture(boolean flagMode) {
        mPreview.takePicture(flagMode);
    }

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */

    public final void setupLayout() {
        mPreview = new CameraV2Preview(appActivity);
        mPreview.setCallback(callback);

        callback.onResultViewInitialized(mPreview);
    }

}
