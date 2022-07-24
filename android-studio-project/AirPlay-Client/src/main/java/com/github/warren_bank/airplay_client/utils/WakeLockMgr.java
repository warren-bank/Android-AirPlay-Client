package com.github.warren_bank.airplay_client.utils;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.PowerManager;

public final class WakeLockMgr {
  private static PowerManager.WakeLock     wakeLock;
  private static WifiManager.WifiLock      wifiLock;
  private static WifiManager.MulticastLock mdnsLock;

  public static int FLAG_WAKELOCK      = 1;
  public static int FLAG_WIFILOCK      = 2;
  public static int FLAG_MULTICASTLOCK = 4;

  public static void acquire(Context context) {
    WakeLockMgr.acquire(context, (WakeLockMgr.FLAG_WAKELOCK | WakeLockMgr.FLAG_WIFILOCK | WakeLockMgr.FLAG_MULTICASTLOCK));
  }

  public static void acquire(Context context, int flags) {
    WakeLockMgr.release(flags);

    PowerManager pm = null;
    WifiManager  wm = null;

    if ((flags & WakeLockMgr.FLAG_WAKELOCK) != 0)
      pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    if (((flags & WakeLockMgr.FLAG_WIFILOCK) != 0) || ((flags & WakeLockMgr.FLAG_MULTICASTLOCK) != 0))
      wm = (WifiManager)  context.getSystemService(Context.WIFI_SERVICE);

    if ((flags & WakeLockMgr.FLAG_WAKELOCK) != 0) {
      wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WakeLock");
      wakeLock.setReferenceCounted(false);
      wakeLock.acquire();
    }

    if ((flags & WakeLockMgr.FLAG_WIFILOCK) != 0) {
      wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "WifiLock");
      wifiLock.setReferenceCounted(false);
      wifiLock.acquire();
    }

    if ((flags & WakeLockMgr.FLAG_MULTICASTLOCK) != 0) {
      mdnsLock = wm.createMulticastLock("WifiMulticastLock");
      mdnsLock.setReferenceCounted(false);
      mdnsLock.acquire();
    }
  }

  public static void release() {
    WakeLockMgr.release((WakeLockMgr.FLAG_WAKELOCK | WakeLockMgr.FLAG_WIFILOCK | WakeLockMgr.FLAG_MULTICASTLOCK));
  }

  public static void release(int flags) {
    if (((flags & WakeLockMgr.FLAG_WAKELOCK) != 0) && (wakeLock != null)) {
      if (wakeLock.isHeld())
        wakeLock.release();
      wakeLock = null;
    }

    if (((flags & WakeLockMgr.FLAG_WIFILOCK) != 0) && (wifiLock != null)) {
      if (wifiLock.isHeld())
        wifiLock.release();
      wifiLock = null;
    }

    if (((flags & WakeLockMgr.FLAG_MULTICASTLOCK) != 0) && (mdnsLock != null)) {
      if (mdnsLock.isHeld())
        mdnsLock.release();
      mdnsLock = null;
    }
  }
}
