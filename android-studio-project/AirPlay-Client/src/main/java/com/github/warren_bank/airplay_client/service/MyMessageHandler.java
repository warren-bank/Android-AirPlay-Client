package com.github.warren_bank.airplay_client.service;

import com.github.warren_bank.airplay_client.constant.Constant;
import com.github.warren_bank.airplay_client.service.AirPlayClient;
import com.github.warren_bank.airplay_client.service.NetworkingService;
import com.github.warren_bank.airplay_client.utils.AirPlayUtils;
import com.github.warren_bank.airplay_client.utils.PreferencesMgr;
import com.github.warren_bank.airplay_client.utils.WakeLockMgr;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URL;

final class MyMessageHandler extends Handler {
  private static final String tag = NetworkingService.class.getSimpleName();

  private Looper                           mainLooper;
  private WeakReference<NetworkingService> networkingServiceRef;
  private WeakReference<AirPlayClient>     airPlayClientRef;

  public MyMessageHandler(Looper looper, NetworkingService service, AirPlayClient client) {
    super(looper);

    mainLooper           = looper;
    networkingServiceRef = new WeakReference<NetworkingService>(service);
    airPlayClientRef     = new WeakReference<AirPlayClient>(client);
  }

  @Override
  public void handleMessage(final Message msg) {
    super.handleMessage(msg);

    final NetworkingService service = networkingServiceRef.get();
    if (service == null) return;

    final AirPlayClient client = airPlayClientRef.get();
    if (client == null) return;

    try {
      switch (msg.what) {

        // =======================================================================
        // Show dialog for user to select one AirPlay receiver from list
        // =======================================================================

        case Constant.Msg.Msg_AirPlay_Show_Connect_Dialog : {
          Context context = (Context) msg.obj;
          service.showConnectDialog(context);
          break;
        }

        // =======================================================================
        // Display visual notification
        // =======================================================================

        case Constant.Msg.Msg_AirPlay_Connect : {
          String receiver_name = (String) msg.obj;
          service.toast("Using AirPlay service: " + receiver_name);
          break;
        }

        // =======================================================================
        // Send raw image
        // =======================================================================

        case Constant.Msg.Msg_Photo : {
          File file = (File) msg.obj;
          client.putImage(file, service.getSelectedService(), AirPlayUtils.getTransition(PreferencesMgr.get_image_transition()));
          break;
        }

        // =======================================================================
        // Send URL to media served by localhost
        // =======================================================================

        case Constant.Msg.Msg_Play : {
          File file = (File) msg.obj;
          URL  url  = new URL("http", service.getNetworkAddress("localhost", false), PreferencesMgr.get_server_port(), Base64.encodeToString(file.getAbsolutePath().getBytes(), Base64.NO_WRAP|Base64.URL_SAFE));
          client.playVideo(url, service.getSelectedService());

          WakeLockMgr.acquire(service, WakeLockMgr.FLAG_WAKELOCK | WakeLockMgr.FLAG_WIFILOCK);
          break;
        }

        // =======================================================================
        // Stop playback of media served by localhost
        // =======================================================================

        case Constant.Msg.Msg_Stop : {
          client.stopVideo(service.getSelectedService());

          WakeLockMgr.release(WakeLockMgr.FLAG_WAKELOCK | WakeLockMgr.FLAG_WIFILOCK);
          break;
        }

        // =======================================================================
        // Stop foreground service
        // =======================================================================

        case Constant.Msg.Msg_Exit_Service : {
          service.stopSelf();
          break;
        }

      }
    }
    catch(Exception e) {}
  }

}
