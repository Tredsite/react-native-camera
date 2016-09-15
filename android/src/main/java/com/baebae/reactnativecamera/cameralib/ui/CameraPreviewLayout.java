package com.baebae.reactnativecamera.cameralib.ui;

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

import com.baebae.reactnativecamera.cameralib.barcode.Scan;
import com.baebae.reactnativecamera.cameralib.helpers.CameraHandlerThread;
import com.baebae.reactnativecamera.cameralib.helpers.CameraUtils;
import com.baebae.reactnativecamera.cameralib.helpers.CameraInstanceManager;
import com.google.zxing.Result;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

public class CameraPreviewLayout extends FrameLayout implements Camera.PreviewCallback  {
    private Camera mCamera;
    private CameraView mPreview;
    private CameraHandlerThread mCameraHandlerThread;
    private Boolean mFlashState;
    private boolean mAutofocusState = true;
    private Scan barcodeScanner = null;
    private FrameLayout cameraLayout = null;
    private CameraInstanceManager cameraInstanceManager;
    private static boolean flagPreviewInitialized = false;


    public CameraPreviewLayout(Context context, CameraInstanceManager cameraInstanceManager) {
        super(context);
        barcodeScanner = new Scan(getContext());
        this.cameraInstanceManager = cameraInstanceManager;
    }

    protected  void changeCameraOrientation(final int orientation, final Runnable callback) {
        CameraView.changeOrientation(orientation);
    }

    public final void setupLayout(Camera camera) {
        mPreview = new CameraView(getContext(), null, camera, this);
        if (cameraLayout == null) {
            removeView(cameraLayout);
        }

        cameraLayout = new FrameLayout(getContext());
        cameraLayout.setBackgroundColor(Color.BLACK);
        cameraLayout.addView(mPreview, 0);

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

    private String getImageFileName() {
        return UUID.randomUUID() + ".jpg";
    }

    public void startCamera(int cameraId) {
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
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mFlashState != null) {
                        setFlash(mFlashState);
                    }
                    flagPreviewInitialized = true;
                    registerLifecycleEventListener();
                }
            }, 1000);
            setAutoFocus(mAutofocusState);
        }
    }

    protected void unregisterLifecycleEventListener() {
    }
    protected void registerLifecycleEventListener() {
    }

    public void startCamera() {
        startCamera(-1);
    }

    public boolean isRunning() {
        return flagPreviewInitialized;
    }
    public void stopCamera() {
        if (!flagPreviewInitialized) {
            return;
        }

        unregisterLifecycleEventListener();
        if (cameraLayout == null && mCamera == null) {
            Log.d("CrashCase", "stopCamera " + this + " " + mPreview + " " + mCamera);
        }
        if (cameraLayout != null) {
            cameraLayout.removeView(mPreview);
            removeView(cameraLayout);
            cameraLayout = null;
        }
        flagPreviewInitialized = false;
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

    public void setFlash(boolean flag) {
        mFlashState = flag;
        if(mCamera != null) {
            toggleTorch(flag);
        }
    }

    public boolean getFlash() {
        if(mCamera != null && CameraUtils.isFlashSupported(mCamera)) {
            Camera.Parameters parameters = mCamera.getParameters();
            if(parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public void toggleTorch(boolean flagTorch) {
        if(mCamera != null && CameraUtils.isFlashSupported(mCamera)) {
            Camera.Parameters parameters = mCamera.getParameters();
            if(!flagTorch) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            } else {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            }
            mCamera.setParameters(parameters);
        }
    }

    public void setAutoFocus(boolean state) {
        mAutofocusState = state;
        if(mPreview != null) {
            mPreview.setAutoFocus(state);
        }
    }

    protected void onImageFileSaved(String imagePath) {

    }

    protected void onBarcodeScanned(Result str) {

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = parameters.getPreviewSize();
        int width = size.width;
        int height = size.height;
        barcodeScanner.scanImage(data, width, height, new Scan.ResultCallback() {
            @Override
            public void onDecodeBarcode(Result result) {
                onBarcodeScanned(result);
            }
        });
    }

    private boolean flagCapturePortraitMode = true;
    Camera.PictureCallback callbackRAW = new Camera.PictureCallback(){
        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {
            // TODO Auto-generated method stub

        }
    };

    public static Bitmap remakeBitmap(Bitmap bm, int newWidth, int newHeight, int rotateAngle, boolean horizontalMirror, boolean verticalMirror) {
        int width = bm.getWidth();
        int height = bm.getHeight();

        //get width, height scale from old one..
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        //apply matrix
        if (horizontalMirror) {
            matrix.postScale(scaleWidth, -scaleHeight);
        } else if (verticalMirror){
            matrix.postScale(-scaleWidth, scaleHeight);
        } else {
            matrix.postScale(scaleWidth, scaleHeight);
        }
        if (rotateAngle != 0) {
            matrix.postRotate(rotateAngle);
        }

        // "RECREATE" THE NEW BITMAP, recycle old bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        if (resizedBitmap != bm) {
            bm.recycle();
        }
        return resizedBitmap;
    }


    PictureCallback callbackImage = new PictureCallback(){
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
                Bitmap newBitmapPicture = remakeBitmap(oldBitmap, oldBitmap.getWidth(), oldBitmap.getHeight(), rotateAngle, false, false);

                String packageName = getContext().getPackageName();
                File externalPath = Environment.getExternalStorageDirectory();
                String dirPath = externalPath.getAbsolutePath() + "/Android/data/" + packageName + "/files/";
                File file = new File(dirPath + getImageFileName());

                File dirFile = new File(dirPath);
                dirFile.mkdirs();
                file.createNewFile();

                FileOutputStream outStream = new FileOutputStream(file);
                newBitmapPicture.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.flush();
                outStream.close();
                newBitmapPicture.recycle();
                onImageFileSaved(file.getAbsolutePath());
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
}
