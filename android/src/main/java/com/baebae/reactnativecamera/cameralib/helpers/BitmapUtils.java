package com.baebae.reactnativecamera.cameralib.helpers;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by baebae on 1/12/16.
 */
public class BitmapUtils {
    /**
     * remake bitmap from original
     * @param bm original bimap
     * @param newWidth width of new bitmap
     * @param newHeight height of new bitmap
     * @param rotateAngle rotate angle from old one to new one
     * @param horizontalMirror flag horizontal mirror
     * @param verticalMirror flag vertical mirror
     * @return new bitmap after resize, rotate, mirror.
     */
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
        bm.recycle();
        return resizedBitmap;
    }
}
