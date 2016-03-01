package com.baebae.reactnativecamera.cameralib.helpers;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

public class DisplayUtils {
    public static int getScreenOrientation(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int orientation = Configuration.ORIENTATION_PORTRAIT;
        Point size = new Point();
        display.getSize(size);
        if(size.x >= size.y){
            orientation = Configuration.ORIENTATION_LANDSCAPE;
        }
        return orientation;
    }

}
