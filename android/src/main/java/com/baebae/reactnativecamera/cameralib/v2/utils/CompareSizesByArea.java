package com.baebae.reactnativecamera.cameralib.v2.utils;

import android.annotation.TargetApi;
import android.util.Size;

import java.util.Comparator;

/**
 * Created by baebae on 3/1/16.
 */
public class CompareSizesByArea implements Comparator<Size> {

    @TargetApi(21)
    @Override
    public int compare(Size lhs, Size rhs) {
        // We cast here to ensure the multiplications won't overflow
        return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
    }

}