package com.baebae.reactnativecamera.cameralib.v2.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.Image;

import com.baebae.reactnativecamera.cameralib.helpers.CameraHelpers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by baebae on 3/1/16.
 */
public class ImageSaver implements Runnable {

    /**
     * The JPEG image
     */
    private final Image mImage;
    private final String filePath;
    public ImageSaver(Image image, String filePath) {
        mImage = image;
        this.filePath = filePath;
    }

    @TargetApi(21)
    @Override
    public void run() {

        ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        FileOutputStream output = null;
        try {
            File file = new File(filePath);
            file.createNewFile();

            output = new FileOutputStream(file);
            output.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mImage.close();
            if (null != output) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}