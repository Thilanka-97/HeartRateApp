package com.heartrateapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class HeartRateModule extends ReactContextBaseJavaModule {
    private OutputAnalyzer analyzer;
    private static ReactApplicationContext reactContext;
    static final AtomicInteger number = new AtomicInteger(0);
    // private Handler mainHandler;
    private static TextureView cameraTextureView;

    private CameraService cameraService;
    private Activity currentActivity;


    HeartRateModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
    }

    public static TimerTask getMessage(String eventName, String eventData) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, eventData);
        return null;
    }

    // @ReactMethod
    // public static void testFunction() {
    //    // test function
    //     HeartRateMainActivity instance = new HeartRateMainActivity();

    //     // Call the non-static method using the instance
    //     instance.newMeasurement();

    // }

    public static void sendHeartRateOutput(String eventName, ArrayList<HeartRateOutputObject> eventData) {
        getMessage("status", "FINISH");
        JSONArray jsonArray = new JSONArray();
        for (HeartRateOutputObject obj : eventData) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("measurement", obj.getMeasurement());
                jsonObject.put("timestamp", obj.getTimestamp());
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, jsonArray.toString());
    }

    @ReactMethod
    public static void emitEvent(String eventData) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                int currentValue = number.getAndIncrement();
                getMessage("hello", eventData + " " + currentValue);
            }
        }, 2000, 2000);
    }

    @ReactMethod
    public void createTextureView(Promise promise) {
        currentActivity = reactContext.getCurrentActivity();

        // Create the TextureView programmatically here
        if (cameraTextureView == null) {
            cameraTextureView = new TextureView(currentActivity);
        }

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(200, 200); // Adjust size as needed
        cameraTextureView.setLayoutParams(layoutParams);

        Log.i("currentActivity", String.valueOf(currentActivity));
        Log.i("currentActivity", "currentActivity");
        // Add the TextureView to the current activity's view hierarchy
        currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewGroup rootView = (ViewGroup) currentActivity.findViewById(android.R.id.content);
                rootView.addView(cameraTextureView);
                promise.resolve(cameraTextureView.getId());
            }
        });
    }

     @ReactMethod
     public void startMeasure(int textureViewId) {
        getMessage("status", "START");
        cameraService = new CameraService(currentActivity);

        // TextureView cameraTextureViewFromJs = new TextureView(reactContext);
        //  HeartRateMainActivity instance = new HeartRateMainActivity();
        //  instance.onClickNewMeasurement();
        analyzer = new OutputAnalyzer(currentActivity);
        // analyzer = new OutputAnalyzer(currentActivity, findViewById(graphId), mainHandler);

        // clear prior results
        // char[] empty = new char[0];
        // ((EditText) findViewById(R.id.editText)).setText(empty, 0, 0);
        // ((TextView) findViewById(R.id.textView)).setText(empty, 0, 0);

         Log.d("TextureView", "textureViewId: " + textureViewId);
        //  TextureView cameraTextureViewFromJs = getCurrentActivity().findViewById(textureViewId);
         if (cameraTextureView != null) {
             Log.d("TextureView", "Found TextureView: " + cameraTextureView);
             // Rest of your code
         } else {
             Log.d("TextureView", "TextureView not found!");
         }

        //TextureView cameraTextureViewFromJs = getCurrentActivity().findViewById(textureViewId);
        Log.i("cameraTextureViewFromJs", String.valueOf(cameraTextureView));
        SurfaceTexture previewSurfaceTexture = cameraTextureView.getSurfaceTexture();
        Log.i("previewSurfaceTexture", String.valueOf(previewSurfaceTexture));

        if (previewSurfaceTexture != null) {
            // this first appears when we close the application and switch back
            // - TextureView isn't quite ready at the first onResume.
            Surface previewSurface = new Surface(previewSurfaceTexture);
            cameraService.start(previewSurface);
            analyzer.measurePulse(cameraTextureView, cameraService);
        }
        //  getCustomView.invoke(cameraTextureViewFromJs);
     }

    @ReactMethod
    public static void emitFinishOutPut(String eventData) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                int currentValue = number.getAndIncrement();
                getMessage("hello", eventData + " " + currentValue);
            }
        }, 2000, 2000);
    }


    @NonNull
    @Override
    public String getName() {
        return "HeartRateModule";
    }

}
