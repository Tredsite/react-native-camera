package com.baebae.reactnativecamera.cameralib.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.baebae.reactnativecamera.cameralib.helpers.DisplayUtils;
import com.facebook.react.uimanager.ViewGroupManager;

import java.util.List;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "CameraPreview";

    private Camera mCamera;
    private Handler mAutoFocusHandler;
    private boolean mPreviewing = true;
    private boolean mAutoFocus = true;
    private boolean mSurfaceCreated = false;
    private Camera.PreviewCallback mPreviewCallback;
    private boolean mFrontCamera = false;

    private Activity appActivity = null;
    private static int orientation = 90;
    public CameraView(Activity context, Camera camera, Camera.PreviewCallback previewCallback) {
        super(context);
        this.appActivity = context;
        init(camera, previewCallback);
    }

    public CameraView(Context context, AttributeSet attrs, Camera camera, Camera.PreviewCallback previewCallback) {
        super(context, attrs);
        init(camera, previewCallback);
    }

    public void init(Camera camera, Camera.PreviewCallback previewCallback) {
        setCamera(camera, previewCallback);
        mAutoFocusHandler = new Handler();
        getHolder().addCallback(this);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void setCamera(Camera camera, Camera.PreviewCallback previewCallback) {
        mCamera = camera;
        mPreviewCallback = previewCallback;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mSurfaceCreated = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        if(surfaceHolder.getSurface() == null) {
            return;
        }
        stopCameraPreview();
        showCameraPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mSurfaceCreated = false;
        stopCameraPreview();
    }

    public void showCameraPreview() {
        if(mCamera != null) {
            try {
                getHolder().addCallback(this);
                mPreviewing = true;
                setupCameraParameters();
                mCamera.setPreviewDisplay(getHolder());
                mCamera.setDisplayOrientation(orientation);
                mCamera.setPreviewCallback(mPreviewCallback);
                mCamera.startPreview();
                if(mAutoFocus) {
                    if (mSurfaceCreated) { // check if surface created before using autofocus
                        safeAutoFocus();
                    } else {
                        scheduleAutoFocus(); // wait 1 sec and then do check again
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }
        }
    }

    public void safeAutoFocus() {
        try {
            mCamera.autoFocus(autoFocusCB);
        } catch (RuntimeException re) {
            scheduleAutoFocus(); // wait 1 sec and then do check again
        }
    }

    public void stopCameraPreview() {
        if(mCamera != null) {
            try {
                mPreviewing = false;
                getHolder().removeCallback(this);
                mCamera.cancelAutoFocus();
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
            } catch(Exception e) {
                Log.e(TAG, e.toString(), e);
            }
        }
    }
    private Camera.Size optimalSize = null;
    public static void changeOrientation(int orientation) {
        CameraView.orientation = orientation;
    }

    protected void changeCameraOrientation(int orientation) {
        changeOrientation(orientation);
        if (mCamera != null) {
            mCamera.setDisplayOrientation(orientation);
            if (optimalSize != null) {
                adjustViewSize(optimalSize, orientation);
            }
        }
    }


    /**
     * setup camera preview, rotation angle correctly.
     */
    public void setupCameraParameters() {
        optimalSize = getBestPreviewSize(1920, 1080);
        Camera.Size pictureSize = getBestPictureSize(1920, 1080);
        Camera.Parameters parameters = mCamera.getParameters();

        parameters.setPreviewSize(optimalSize.width, optimalSize.height);
        parameters.setPictureSize(pictureSize.width, pictureSize.height);
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
//        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//            parameters.setRotation(270);
//        } else {
//            parameters.setRotation(90);
//        }

        mCamera.setParameters(parameters);
        adjustViewSize(optimalSize, orientation);
    }

    private void adjustViewSize(Camera.Size cameraSize, int orientation) {

        Display display = appActivity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int parentWidth = size.x;
        int parentHeight = size.y;

        Point ptCameraSize = convertSizeToLandscapeOrientation(new Point(cameraSize.width, cameraSize.height));
        if (orientation == 90) {
            float cameraRatio = ((float) ptCameraSize.x) / ptCameraSize.y;
            int width = parentWidth;
            if (parentWidth > parentHeight) {
                width = parentHeight;
            }

            int height = (int)(width / cameraRatio);
            setViewSize(width, height);
        } else {
            float cameraRatio = ((float) ptCameraSize.x) / ptCameraSize.y;
            int height = parentWidth;
            if (parentWidth > parentHeight) {
                height = parentHeight;
            }
            int width = (int)(height * cameraRatio);
            setViewSize(height, width);
        }

    }

    private Point convertSizeToLandscapeOrientation(Point size) {
        if (getCameraOrientation() % 180 == 0) {
            return size;
        } else {
            return new Point(size.y, size.x);
        }
    }

    private void setViewSize(int width, int height) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)getLayoutParams();
        FrameLayout.LayoutParams parentParams = (FrameLayout.LayoutParams)((View)getParent()).getLayoutParams();
        ViewGroup.LayoutParams parentParentParms =((View)getParent().getParent()).getLayoutParams();
        if (getDisplaySurfaceOrientation() % 180 == 0) {
            layoutParams.width = width;
            layoutParams.height = height;

            parentParams.width = width;
            parentParams.height = height;
            parentParams.gravity = Gravity.LEFT | Gravity.TOP;
            layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        } else {
            layoutParams.width = height;
            layoutParams.height = width;

            parentParams.width = height;
            parentParams.height = width;
            parentParams.gravity = Gravity.LEFT | Gravity.TOP;
            layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        }
        int parentHeight = ((View)getParent()).getHeight();

        layoutParams.topMargin = (parentHeight - height) / 2;
        setLayoutParams(layoutParams);
        ((View)getParent()).setLayoutParams(parentParams);

        ViewGroup.LayoutParams param = ((View)getParent().getParent()).getLayoutParams();
        param.width = parentParams.width;
        param.height = parentParams.height;
        ((View)getParent().getParent()).setLayoutParams(param);
    }


    private int getDisplaySurfaceOrientation() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }
        return degrees;
    }

    private int getCameraOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);

        int degrees = getDisplaySurfaceOrientation();
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    private Camera.Size getBestPreviewSize(int width, int height)
    {
        Camera.Size result=null;
        if(mCamera == null) {
            return null;
        }
        Camera.Parameters p = mCamera.getParameters();
        for (Camera.Size size : p.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result==null) {
                    result=size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }
        return result;

    }

    private Camera.Size getBestPictureSize(int width, int height)
    {
        Camera.Size result=null;
        if(mCamera == null) {
            return null;
        }
        Camera.Parameters p = mCamera.getParameters();
        for (Camera.Size size : p.getSupportedPictureSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result==null) {
                    result=size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }
        return result;

    }
    private Camera.Size getOptimalPreviewSize() {
        if(mCamera == null) {
            return null;
        }
        List<Camera.Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
        int w = getWidth();
        int h = getHeight();
        if (DisplayUtils.getScreenOrientation(getContext()) == Configuration.ORIENTATION_PORTRAIT) {
            int portraitWidth = h;
            h = w;
            w = portraitWidth;
        }

        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void setAutoFocus(boolean state) {
        if(mCamera != null && mPreviewing) {
            if(state == mAutoFocus) {
                return;
            }
            mAutoFocus = state;
            if(mAutoFocus) {
                if (mSurfaceCreated) { // check if surface created before using autofocus
                    Log.v(TAG, "Starting autofocus");
                    safeAutoFocus();
                } else {
                    scheduleAutoFocus(); // wait 1 sec and then do check again
                }
            } else {
                Log.v(TAG, "Cancelling autofocus");
                mCamera.cancelAutoFocus();
            }
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if(mCamera != null && mPreviewing && mAutoFocus && mSurfaceCreated) {
                safeAutoFocus();
            }
        }
    };

    // Mimic continuous auto-focusing
    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            scheduleAutoFocus();
        }
    };

    private void scheduleAutoFocus() {
        mAutoFocusHandler.postDelayed(doAutoFocus, 3000);
    }
}
