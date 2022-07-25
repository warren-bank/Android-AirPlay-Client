package com.github.warren_bank.airplay_client.mirror;

import static com.github.warren_bank.airplay_client.mirror.ScreenMirrorMgr.getAppData;

import com.github.warren_bank.airplay_client.MainApp;

final class ImageDispatcher {
  private final Object mLock = new Object();
  private JpegStreamerThread mJpegStreamerThread;
  private volatile boolean isThreadRunning;

  private class JpegStreamerThread extends Thread {
    private byte[] mCurrentJpeg;
    private byte[] mLastJpeg;
    private int mSleepCount;

    JpegStreamerThread() {
      super(JpegStreamerThread.class.getSimpleName());
    }

    public void run() {
      while (!isInterrupted()) {
        if (!isThreadRunning) break;
        mCurrentJpeg = getAppData().getImageQueue().poll();
        if (mCurrentJpeg == null) {
          try {
            sleep(24);
          } catch (InterruptedException ignore) {
            continue;
          }
          mSleepCount++;
          if (mSleepCount >= 20) sendLastJPEGToClients();
        } else {
          mLastJpeg = mCurrentJpeg;
          sendLastJPEGToClients();
        }
      }
    }

    private void sendLastJPEGToClients() {
      mSleepCount = 0;
      synchronized (mLock) {
        if (!isThreadRunning) return;
        try {
          getAppData().getClient().putRawImage(
            mLastJpeg,
            MainApp.getNetworkingService().getSelectedService(),
            /* transition */ "None"
          );
        }
        catch(Exception e) {}
      }
    }
  }

  void start() {
    synchronized (mLock) {
      if (isThreadRunning) return;
      mJpegStreamerThread = new JpegStreamerThread();
      mJpegStreamerThread.start();
      isThreadRunning = true;
    }
  }

  void stop(final byte[] clientNotifyImage) {
    synchronized (mLock) {
      if (!isThreadRunning) return;
      isThreadRunning = false;
      mJpegStreamerThread.interrupt();

      try {
        if ((clientNotifyImage != null) && (clientNotifyImage.length > 0)) {
          getAppData().getClient().putRawImage(
            clientNotifyImage,
            MainApp.getNetworkingService().getSelectedService(),
            /* transition */ "None"
          );
        }
      }
      catch(Exception e) {}
    }
  }
}
