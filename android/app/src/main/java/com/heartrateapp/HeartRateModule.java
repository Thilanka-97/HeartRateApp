package com.heartrateapp;

import android.content.Intent;

import androidx.annotation.NonNull;

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

    private static ReactApplicationContext reactContext;
    static final AtomicInteger number = new AtomicInteger(0);
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

    @ReactMethod
    public static void testFunction() {
       // test function
        HeartRateMainActivity instance = new HeartRateMainActivity();

        // Call the non-static method using the instance
        instance.newMeasurement();

    }

    public static void sendHeartRateOutput(String eventName, ArrayList<HeartRateOutputObject> eventData) {
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
     public void startMeasure(int Id) {
         // CameraPreviewView cameraPreviewView = new CameraPreviewView(reactContext);
         // // Initialize and configure your camera service here, e.g., start the camera and pass the preview surface to the view
         // // ...
         // HeartRateMainActivity instance = new HeartRateMainActivity();
         HeartRateMainActivity instance = new HeartRateMainActivity();

         // Call the non-static method using the instance
         instance.onClickNewMeasurement();

         // promise.resolve(cameraPreviewView.getId());
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

    @ReactMethod
    public void openXmlLayoutActivity() {
        Intent intent = new Intent(getReactApplicationContext(), HeartRateMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getReactApplicationContext().startActivity(intent);
    }

    @NonNull
    @Override
    public String getName() {
        return "HeartRateModule";
    }

}
