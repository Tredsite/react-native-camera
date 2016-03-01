package com.baebae.reactnativecamera.cameralib.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Environment;

import java.io.File;
import java.util.UUID;

/**
 * Created by baebae on 3/1/16.
 */
public class CameraHelpers {
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

    public static String getImageFileName() {
        return UUID.randomUUID() + ".jpg";
    }

    public static String getDirPath(Context context) {
        String packageName = context.getPackageName();
        File externalPath = Environment.getExternalStorageDirectory();
        String dirPath = externalPath.getAbsolutePath() + "/Android/data/" + packageName + "/files/";

        File dirFile = new File(dirPath);
        dirFile.mkdirs();

        return dirPath;
    }
}
