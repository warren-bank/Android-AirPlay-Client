package com.github.warren_bank.airplay_client;

import com.github.warren_bank.airplay_client.service.NetworkingService;

import android.app.Application;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

public class MainApp extends Application {
  private static MainApp instance = null;
  private static WeakReference<NetworkingService> serviceRef = null;
  public static String receiverName = null;

  private ConcurrentHashMap<String, Handler> mHandlerMap = new ConcurrentHashMap<String, Handler>();

  public static MainApp getInstance() {
    return instance;
  }

  public static NetworkingService getNetworkingService() {
    return (serviceRef != null)
      ? serviceRef.get()
      : null;
  }

  public static void setNetworkingService(NetworkingService service) {
    serviceRef = (service != null)
      ? new WeakReference<NetworkingService>(service)
      : null;
  }

  public static void registerHandler(String name, Handler handler) {
    getInstance().getHandlerMap().put(name, handler);
  }

  public static void unregisterHandler(String name) {
    getInstance().getHandlerMap().remove(name);
  }

  public static void broadcastMessage(Message msg) {
    for (Handler handler : getInstance().getHandlerMap().values()) {
      handler.sendMessage(Message.obtain(msg));
    }
  }

  public static void broadcastMessageDelayed(Message msg, long delayMillis) {
    if (delayMillis <= 0L) {
      broadcastMessage(msg);
      return;
    }

    for (Handler handler : getInstance().getHandlerMap().values()) {
      handler.sendMessageDelayed(Message.obtain(msg), delayMillis);
    }
  }

  public static void cancelMessages(int what) {
    for (Handler handler : getInstance().getHandlerMap().values()) {
      handler.removeMessages(what);
    }
  }

  @Override
  public void onCreate() {
    super.onCreate();
    instance = this;
  }

  public ConcurrentHashMap<String, Handler> getHandlerMap() {
    return mHandlerMap;
  }
}
