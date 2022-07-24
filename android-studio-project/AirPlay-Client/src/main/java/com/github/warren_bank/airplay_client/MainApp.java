package com.github.warren_bank.airplay_client;

import android.app.Application;
import android.os.Handler;
import android.os.Message;

import java.util.concurrent.ConcurrentHashMap;

public class MainApp extends Application {
  private static MainApp instance;

  private ConcurrentHashMap<String, Handler> mHandlerMap = new ConcurrentHashMap<String, Handler>();

  public static MainApp getInstance() {
    return instance;
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

  @Override
  public void onCreate() {
    super.onCreate();
    instance = this;
  }

  public ConcurrentHashMap<String, Handler> getHandlerMap() {
    return mHandlerMap;
  }
}
