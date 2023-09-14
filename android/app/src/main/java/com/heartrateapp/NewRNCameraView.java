package com.heartrateapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.*;
import android.widget.FrameLayout;

import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleObserver;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerModule;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewRNCameraView extends FrameLayout implements LifecycleObserver {
  private final ThemedReactContext currentContext;
  private final PreviewView viewFinder;

  public NewRNCameraView(ThemedReactContext context) {
    super(context);
    currentContext = context;
    viewFinder = new PreviewView(context);
    installHierarchyFitter(viewFinder);
    addView(viewFinder);
  }

  private void installHierarchyFitter(ViewGroup view) {
    if (getContext() instanceof ThemedReactContext) {
      view.setOnHierarchyChangeListener(new OnHierarchyChangeListener() {
        @Override
        public void onChildViewAdded(View parent, View child) {
          parent.measure(
                  MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
                  MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY)
          );
              parent.layout(0, 0, parent.getMeasuredWidth(), parent.getMeasuredHeight());
              Log.i("onChildViewAdded", String.valueOf(child));
        }

        @Override
        public void onChildViewRemoved(View parent, View child) {
        }
      });
    }
  }

  public int getViewId() {
    return viewFinder.getId();
  }


  private boolean hasPermissions() {
    String[] requiredPermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    for (String permission : requiredPermissions) {
      if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
        return false;
      }
    }
    return true;
  }
}
