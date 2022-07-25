package com.github.warren_bank.airplay_client.mirror;

import com.github.warren_bank.airplay_client.MainApp;
import com.github.warren_bank.airplay_client.constant.Constant;

import static android.view.Surface.ROTATION_0;
import static android.view.Surface.ROTATION_180;

import android.content.Context;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import android.util.Log;

public class ScreenMirrorMgr {

  private static final String TAG = "ScreenMirrorMgr";
  private static ScreenMirrorMgr sAppInstance = null;

  private Context       context;
  private AppData       appData;
  private HandlerThread handlerThread;
  private Handler       handler;

  private MediaProjectionManager   mMediaProjectionManager;
  private MediaProjection          mMediaProjection;
  private MediaProjection.Callback mProjectionCallback;

  private ScreenMirrorMgr(Context context) {
    sAppInstance = this;

    this.context = context;

    appData = new AppData(context);

    handlerThread = new HandlerThread(
      ScreenMirrorMgr.class.getSimpleName(),
      Process.THREAD_PRIORITY_MORE_FAVORABLE
    );
    handlerThread.start();

    handler = new ScreenMirrorHandler(handlerThread.getLooper());
    MainApp.registerHandler(ScreenMirrorMgr.class.getName(), handler);

    mMediaProjectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    mMediaProjection        = null;
    mProjectionCallback     = new MediaProjection.Callback() {
      @Override
      public void onStop() {
        Message msg = Message.obtain();
        msg.what = Constant.Msg.Msg_ScreenMirror_Stream_Stop;
        MainApp.broadcastMessage(msg);
      }
    };
  }

  private void startStreaming() {
    if (appData.isStreamRunning()) return;

    if (mMediaProjection != null)
      mMediaProjection.registerCallback(mProjectionCallback, null);
  }

  private void stopStreaming() {
    if (!appData.isStreamRunning()) return;

    if (mMediaProjection != null) {
      mMediaProjection.unregisterCallback(mProjectionCallback);
      mMediaProjection.stop();
    }

    appData.getImageQueue().clear();
  }

  private boolean getOrientation() {
    final int rotation = appData.getWindowsManager().getDefaultDisplay().getRotation();
    return (rotation == ROTATION_0) || (rotation == ROTATION_180);
  }

  public static ScreenMirrorMgr getInstance(Context context) {
    return (sAppInstance == null)
      ? new ScreenMirrorMgr(context)
      : sAppInstance;
  }

  public static AppData getAppData() {
    return (sAppInstance == null) ? null : sAppInstance.appData;
  }

  public static MediaProjectionManager getProjectionManager() {
    return (sAppInstance == null) ? null : sAppInstance.mMediaProjectionManager;
  }

  public static void setMediaProjection(final MediaProjection mediaProjection) {
    if (sAppInstance != null)
      sAppInstance.mMediaProjection = mediaProjection;
  }

  public static MediaProjection getMediaProjection() {
    return (sAppInstance == null) ? null : sAppInstance.mMediaProjection;
  }

  // ---------------------------------------------------------------------------
  // global Message listener

  private class ScreenMirrorHandler extends Handler {
    private final ImageDispatcher mImageDispatcher;
    private final ImageGenerator  mImageGenerator;

    private boolean mCurrentOrientation;

    ScreenMirrorHandler(final Looper looper) {
      super(looper);
      mImageDispatcher = new ImageDispatcher();
      mImageGenerator  = new ImageGenerator();
    }

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);

      switch (msg.what) {
        case Constant.Msg.Msg_ScreenMirror_Stream_Start : {
          if (appData.isStreamRunning()) break;

          MainApp.cancelMessages(Constant.Msg.Msg_ScreenMirror_Detect_Rotation);

          msg = Message.obtain();
          msg.what = Constant.Msg.Msg_ScreenMirror_Detect_Rotation;
          MainApp.broadcastMessageDelayed(msg, 250);

          startStreaming();
          mImageDispatcher.start();
          mImageGenerator.start();
          mCurrentOrientation = getOrientation();
          appData.setStreamRunning(true);
          break;
        }
        case Constant.Msg.Msg_ScreenMirror_Stream_Pause : {
          if (!appData.isStreamRunning()) break;

          msg = Message.obtain();
          msg.what = Constant.Msg.Msg_ScreenMirror_Stream_Resume;
          MainApp.broadcastMessageDelayed(msg, 250);

          mImageGenerator.stop();
          break;
        }
        case Constant.Msg.Msg_ScreenMirror_Stream_Resume : {
          if (!appData.isStreamRunning()) break;

          msg = Message.obtain();
          msg.what = Constant.Msg.Msg_ScreenMirror_Detect_Rotation;
          MainApp.broadcastMessageDelayed(msg, 250);

          mImageGenerator.start();
          break;
        }
        case Constant.Msg.Msg_ScreenMirror_Stream_Stop : {
          if (!appData.isStreamRunning()) break;

          MainApp.cancelMessages(Constant.Msg.Msg_ScreenMirror_Detect_Rotation);
          MainApp.cancelMessages(Constant.Msg.Msg_ScreenMirror_Stream_Stop);

          mImageGenerator.stop();
          mImageDispatcher.stop(null);
          stopStreaming();
          appData.setStreamRunning(false);
          break;
        }
        case Constant.Msg.Msg_ScreenMirror_Detect_Rotation : {
          if (!appData.isStreamRunning()) break;

          final boolean newOrientation = getOrientation();
          if (mCurrentOrientation != newOrientation) {
            mCurrentOrientation = newOrientation;

            msg = Message.obtain();
            msg.what = Constant.Msg.Msg_ScreenMirror_Stream_Pause;
            MainApp.broadcastMessage(msg);
          }
          else {
            msg = Message.obtain();
            msg.what = Constant.Msg.Msg_ScreenMirror_Detect_Rotation;
            MainApp.broadcastMessageDelayed(msg, 250);
          }
          break;
        }
        case Constant.Msg.Msg_Stop :
        case Constant.Msg.Msg_Exit_Service : {
          msg = Message.obtain();
          msg.what = Constant.Msg.Msg_ScreenMirror_Stream_Stop;
          MainApp.broadcastMessage(msg);
          break;
        }
      }

    }
  }

}
