package com.baebae.reactnativecamera;

import android.app.Activity;
import android.util.Log;
import com.facebook.react.bridge.ReactApplicationContext;

import com.facebook.react.bridge.ReactContextBaseJavaModule;

public class ApplicationActivity extends ReactContextBaseJavaModule {
    final private Activity mActivity;

    public ApplicationActivity(ReactApplicationContext reactContext) {
        super(reactContext);
        mActivity = getCurrentActivity();
    }

    @Override
    public String getName() {
        return "ApplicationActivity";
    }

    public Activity getActivity() {
        return mActivity;
    }
}
