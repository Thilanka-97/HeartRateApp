package com.heartrateapp;

import static com.heartrateapp.HeartRateModule.getMessage;
import static com.heartrateapp.HeartRateModule.sendHeartRateOutput;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.TextureView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

class OutputAnalyzer {
    private final Activity activity;

    // private final ChartDrawer chartDrawer;

    private MeasureStore store;

    private final int measurementInterval = 45;
    private final int measurementLength = 15000; // ensure the number of data points is the power of two
    private final int clipLength = 3500;

    private int detectedValleys = 0;
    private int ticksPassed = 0;

    private final CopyOnWriteArrayList<Long> valleys = new CopyOnWriteArrayList<>();

    private CountDownTimer timer;

  //  private final Handler mainHandler;

    // OutputAnalyzer(Activity activity, TextureView graphTextureView, Handler mainHandler) {
    OutputAnalyzer(Activity activity) {

        this.activity = activity;
        // this.chartDrawer = new ChartDrawer(graphTextureView);
        // this.mainHandler = mainHandler;
    }

    private boolean detectValley() {
        final int valleyDetectionWindowSize = 13;
        CopyOnWriteArrayList<Measurement<Integer>> subList = store.getLastStdValues(valleyDetectionWindowSize);
        if (subList.size() < valleyDetectionWindowSize) {
            return false;
        } else {
            Integer referenceValue = subList.get((int) Math.ceil(valleyDetectionWindowSize / 2f)).measurement;

            for (Measurement<Integer> measurement : subList) {
                if (measurement.measurement < referenceValue) return false;
            }

            // filter out consecutive measurements due to too high measurement rate
            return (!subList.get((int) Math.ceil(valleyDetectionWindowSize / 2f)).measurement.equals(
                    subList.get((int) Math.ceil(valleyDetectionWindowSize / 2f) - 1).measurement));
        }
    }

    void measurePulse(TextureView textureView, CameraService cameraService) {
        getMessage("status", "RUNNING");
        // 20 times a second, get the amount of red on the picture.
        // detect local minimums, calculate pulse.

        store = new MeasureStore();

        detectedValleys = 0;

        timer = new CountDownTimer(measurementLength, measurementInterval) {

            @Override
            public void onTick(long millisUntilFinished) {
                // skip the first measurements, which are broken by exposure metering
                if (clipLength > (++ticksPassed * measurementInterval))
                    return;

                Thread thread = new Thread(() -> {
                    Bitmap currentBitmap = textureView.getBitmap();
                    int pixelCount = textureView.getWidth() * textureView.getHeight();
                    int measurement = 0;
                    int[] pixels = new int[pixelCount];

                    currentBitmap.getPixels(pixels, 0, textureView.getWidth(), 0, 0, textureView.getWidth(),
                            textureView.getHeight());

                    // extract the red component
                    // https://developer.android.com/reference/android/graphics/Color.html#decoding
                    for (int pixelIndex = 0; pixelIndex < pixelCount; pixelIndex++) {
                        measurement += (pixels[pixelIndex] >> 16) & 0xff;
                    }
                    // max int is 2^31 (2147483647) , so width and height can be at most 2^11,
                    // as 2^8 * 2^11 * 2^11 = 2^30, just below the limit

                    store.add(measurement);

                    if (detectValley()) {
                        Log.i("Detected Valley", String.valueOf(detectedValleys + 1));
                        vibrate(activity, 1);
                        detectedValleys = detectedValleys + 1;
                        valleys.add(store.getLastTimestamp().getTime());
                        // in 13 seconds (13000 milliseconds), I expect 15 valleys. that would be a pulse of 15 / 130000 * 60 * 1000 = 69

                        float pulse = (valleys.size() == 1)
                                ? (60f * (detectedValleys)
                                / (Math.max(1,
                                (measurementLength - millisUntilFinished - clipLength)
                                        / 1000f)))
                                : (60f * (detectedValleys - 1)
                                / (Math.max(1,
                                (valleys.get(valleys.size() - 1) - valleys.get(0)) / 1000f)));

                        String currentValue = String.format(
                                Locale.getDefault(),
                                activity.getResources().getQuantityString(R.plurals.measurement_output_template,
                                        detectedValleys),
                                pulse,
                                detectedValleys,
                                1f * (measurementLength - millisUntilFinished - clipLength) / 1000f);
                        getMessage("full_string", currentValue);
                        getMessage("pulse", String.valueOf(pulse));

                       // sendMessage(activity.MESSAGE_UPDATE_REALTIME, currentValue);
                    }

                    // draw the chart on a separate thread.
                    // Thread chartDrawerThread = new Thread(() -> chartDrawer.draw(store.getStdValues()));
                    // chartDrawerThread.start();
                });
                thread.start();
            }

            public void vibrate(Context context, long milliseconds) {
                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        // Newer Android versions require a VibrationEffect
                        vibrator.vibrate(android.os.VibrationEffect.createOneShot(milliseconds,
                                android.os.VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        // For older Android versions
                        vibrator.vibrate(milliseconds);
                    }
                }
            }


            @Override
            public void onFinish() {
                CopyOnWriteArrayList<Measurement<Float>> stdValues = store.getStdValues();

                // clip the interval to the first till the last one - on this interval, there were detectedValleys - 1 periods

                // If the camera only provided a static image, there are no valleys in the signal.
                // A camera not available error is shown, which is the most likely cause.
                if (valleys.size() == 0) {
                    // mainHandler.sendMessage(Message.obtain(
                    //        mainHandler,
                    //        activity.MESSAGE_CAMERA_NOT_AVAILABLE,
                    //       "No valleys detected - there may be an issue when accessing the camera."));
                    return;
                }

                // String currentValue = String.format(
                //         Locale.getDefault(),
                //         activity.getResources().getQuantityString(R.plurals.measurement_output_template, detectedValleys - 1),
                //         60f * (detectedValleys - 1) / (Math.max(1, (valleys.get(valleys.size() - 1) - valleys.get(0)) / 1000f)),
                //         detectedValleys - 1,
                //         1f * (valleys.get(valleys.size() - 1) - valleys.get(0)) / 1000f);

                // sendMessage(HeartRateMainActivity.MESSAGE_UPDATE_REALTIME, currentValue);

                StringBuilder returnValueSb = new StringBuilder();
                //  returnValueSb.append(currentValue);
                returnValueSb.append(activity.getString(R.string.row_separator));

                // look for "drops" of 0.15 - 0.75 in the value
                // a drop may take 2-3 ticks.
                // int dropCount = 0;
                // for (int stdValueIdx = 4; stdValueIdx < stdValues.size(); stdValueIdx++) {
                //     if (((stdValues.get(stdValueIdx - 2).measurement - stdValues.get(stdValueIdx).measurement) > dropHeight) &&
                //             !((stdValues.get(stdValueIdx - 3).measurement - stdValues.get(stdValueIdx - 1).measurement) > dropHeight) &&
                //            !((stdValues.get(stdValueIdx - 4).measurement - stdValues.get(stdValueIdx - 2).measurement) > dropHeight)
                //    ) {
                //        dropCount++;
                //    }
                // }

                // returnValueSb.append(activity.getString(R.string.detected_pulse));
                // returnValueSb.append(activity.getString(R.string.separator));
                // returnValueSb.append((float) dropCount / ((float) (measurementLength - clipLength) / 1000f / 60f));
                // returnValueSb.append(activity.getString(R.string.row_separator));

                returnValueSb.append(activity.getString(R.string.raw_values));
                returnValueSb.append(activity.getString(R.string.row_separator));

                ArrayList<HeartRateOutputObject> stringList = new ArrayList<>();


                for (int stdValueIdx = 0; stdValueIdx < stdValues.size(); stdValueIdx++) {
                    // stdValues.forEach((value) -> { // would require API level 24 instead of 21.
                    Measurement<Float> value = stdValues.get(stdValueIdx);
                    HeartRateOutputObject obj = new HeartRateOutputObject(value.measurement, value.timestamp);
                    stringList.add(obj);
                   // stringList.add(new HeartRateOutputObject(value.measurement, value.timestamp));
                    String timeStampString =
                            new SimpleDateFormat(
                                    activity.getString(R.string.dateFormatGranular),
                                    Locale.getDefault()
                            ).format(value.timestamp);
                    returnValueSb.append(timeStampString);
                    returnValueSb.append(activity.getString(R.string.separator));
                    returnValueSb.append(value.measurement);
                    returnValueSb.append(activity.getString(R.string.row_separator));
                }

                sendHeartRateOutput("onFinish", stringList);




                returnValueSb.append(activity.getString(R.string.output_detected_peaks_header));
                returnValueSb.append(activity.getString(R.string.row_separator));

                // add detected valleys location
                for (long tick : valleys) {
                    returnValueSb.append(tick);
                    returnValueSb.append(activity.getString(R.string.row_separator));
                }

                // sendMessage(HeartRateMainActivity.MESSAGE_UPDATE_FINAL, returnValueSb.toString());

                cameraService.stop();
            }
        };

        // activity.setViewState(HeartRateMainActivity.VIEW_STATE.MEASUREMENT);
        timer.start();
    }

    void stop() {
        getMessage("status", "STOP");
        if (timer != null) {
            timer.cancel();
        }
    }

    void sendMessage(int what, Object message) {
        Message msg = new Message();
        msg.what = what;
        msg.obj = message;
        // mainHandler.sendMessage(msg);
    }
}
