package com.heartrateapp;

import android.view.TextureView;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;

public class CameraViewManager extends ViewGroupManager<NewRNCameraView> {
    private NewRNCameraView cameraView;
    private static ReactApplicationContext reactContext;

    public CameraViewManager(ReactApplicationContext context) {
        reactContext = context;
    }

    @NonNull
    @Override
    public String getName() {
        return "RNCameraView";
    }

    @Override
    public NewRNCameraView createViewInstance(ThemedReactContext context) {
    //    textureView.setSurfaceTextureListener(textureListener);
    return new NewRNCameraView(context);
    }
}
