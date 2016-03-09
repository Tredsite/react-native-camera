package com.baebae.reactnativecamera.cameralib.v1;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.baebae.reactnativecamera.barcode.Scan;
import com.baebae.reactnativecamera.cameralib.CameraCallback;
import com.baebae.reactnativecamera.cameralib.helpers.CameraHelpers;
import com.baebae.reactnativecamera.cameralib.v1.utils.CameraHandlerThread;
import com.baebae.reactnativecamera.cameralib.v1.utils.CameraInstanceManager;
import com.baebae.reactnativecamera.cameralib.v1.utils.CameraUtils;
import com.google.zxing.Result;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by baebae on 3/1/16.
 */
public class CameraV1Container implements Camera.PreviewCallback {
    private Camera mCamera;
    private CameraView mPreview;
    private CameraHandlerThread mCameraHandlerThread;
    private Boolean mFlashState;
    private boolean mAutoFocusState = true;
    private CameraInstanceManager cameraInstanceManager;
    private View parentView;
    private Activity appActivity = null;
    private Scan barcodeScanner = null;
    private CameraCallback callback = null;

    public CameraV1Container(CameraInstanceManager cameraInstanceManager, View parentView, Activity appActivity, Scan barcodeScanner) {
        this.cameraInstanceManager = cameraInstanceManager;
        this.parentView = parentView;
        this.appActivity = appActivity;
        callback = (CameraCallback)parentView;
        this.barcodeScanner = barcodeScanner;
    }

    public void startCamera() {
        if(mCameraHandlerThread == null) {
            mCameraHandlerThread = new CameraHandlerThread(this);
        }
        Camera camera = cameraInstanceManager.getCamera("back");
        mCameraHandlerThread.startCamera(camera);
    }

    public void setupCameraPreview(Camera camera) {
        mCamera = camera;
        if(mCamera != null) {
            setupLayout(mCamera);
            parentView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mFlashState != null) {
                        setFlash(mFlashState);
                    }
                    callback.onResultCameraInitialized();
                }
            }, 1000);
            setAutoFocus(mAutoFocusState);
        }
    }

    public void stopCamera() {
        if (mCamera != null) {
            mPreview.stopCameraPreview();
            mPreview.setCamera(null, null);

            this.cameraInstanceManager.releaseCamera(mCamera);
            mCamera = null;
        }
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

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = parameters.getPreviewSize();
        int width = size.width;
        int height = size.height;
        callback.onPreviewImage(data, width, height);

    }

    private boolean flagCapturePortraitMode = true;
    Camera.PictureCallback callbackRAW = new Camera.PictureCallback(){
        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {
            // TODO Auto-generated method stub

        }
    };

    Camera.PictureCallback callbackImage = new Camera.PictureCallback(){
        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {
            try {
                Bitmap oldBitmap = BitmapFactory.decodeByteArray(arg0, 0, arg0.length);
                int rotateAngle = 0;
                if (flagCapturePortraitMode) {
                    if (oldBitmap.getWidth() > oldBitmap.getHeight()) {
                        rotateAngle = 90;
                    }
                } else {
                    if (oldBitmap.getWidth() < oldBitmap.getHeight()) {
                        rotateAngle = 90;
                    }
                }
                Bitmap newBitmapPicture = CameraHelpers.remakeBitmap(oldBitmap, oldBitmap.getWidth(), oldBitmap.getHeight(), rotateAngle, false, false);

                File file = new File(CameraHelpers.getDirPath(parentView.getContext()) + CameraHelpers.getImageFileName());
                file.createNewFile();

                FileOutputStream outStream = new FileOutputStream(file);
                newBitmapPicture.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.flush();
                outStream.close();
                newBitmapPicture.recycle();
                callback.onResultImageCaptured(file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void takePicture(boolean flagMode) {
        flagCapturePortraitMode = flagMode;
        mCamera.takePicture(new Camera.ShutterCallback() {
            @Override
            public void onShutter() {

            }
        }, callbackRAW, callbackImage);
    }

    public void setAutoFocus(boolean state) {
        mAutoFocusState = state;
        if(mPreview != null) {
            mPreview.setAutoFocus(state);
        }
    }

    public void setFlash(boolean flag) {
        mFlashState = flag;
        if(mCamera != null) {
            toggleTorch(flag);
        }
    }

    public final void setupLayout(Camera camera) {
        mPreview = new CameraView(appActivity, camera, this);
        callback.onResultViewInitialized(mPreview);
    }
}
