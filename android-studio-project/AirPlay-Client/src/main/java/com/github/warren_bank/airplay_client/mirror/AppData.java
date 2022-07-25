package com.github.warren_bank.airplay_client.mirror;

import com.github.warren_bank.airplay_client.httpclient.AirPlayClient;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.util.concurrent.ConcurrentLinkedDeque;

public final class AppData {
  private final AirPlayClient                 client;
  private final ConcurrentLinkedDeque<byte[]> mImageQueue;
  private final WindowManager                 mWindowManager;
  private final int                           mDensityDpi;
  private final float                         mScale;

  private volatile boolean isStreamRunning;

  public AppData(final Context context) {
    client          = new AirPlayClient();
    mImageQueue     = new ConcurrentLinkedDeque<>();
    mWindowManager  = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    mDensityDpi     = getDensityDpi();
    mScale          = getScale(context);
    isStreamRunning = false;
  }

  public void setStreamRunning(final boolean streamRunning) {
    isStreamRunning = streamRunning;
  }

  public boolean isStreamRunning() {
    return isStreamRunning;
  }

  public AirPlayClient getClient() {
    return client;
  }

  public ConcurrentLinkedDeque<byte[]> getImageQueue() {
    return mImageQueue;
  }

  public WindowManager getWindowsManager() {
    return mWindowManager;
  }

  public int getScreenDensity() {
    return mDensityDpi;
  }

  public float getDisplayScale() {
    return mScale;
  }

  public Point getScreenSize() {
    final Point screenSize = new Point();
    mWindowManager.getDefaultDisplay().getRealSize(screenSize);
    return screenSize;
  }

  //Private
  private int getDensityDpi() {
    final DisplayMetrics displayMetrics = new DisplayMetrics();
    mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
    return displayMetrics.densityDpi;
  }

  private float getScale(final Context context) {
    return context.getResources().getDisplayMetrics().density;
  }
}
