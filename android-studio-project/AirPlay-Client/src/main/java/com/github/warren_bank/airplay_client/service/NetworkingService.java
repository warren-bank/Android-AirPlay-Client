package com.github.warren_bank.airplay_client.service;

import com.github.warren_bank.airplay_client.MainApp;
import com.github.warren_bank.airplay_client.R;
import com.github.warren_bank.airplay_client.constant.Constant;
import com.github.warren_bank.airplay_client.httpclient.AirPlayClient;
import com.github.warren_bank.airplay_client.httpclient.AirPlayClientCallback;
import com.github.warren_bank.airplay_client.httpd.HttpServer;
import com.github.warren_bank.airplay_client.mirror.ScreenMirrorMgr;
import com.github.warren_bank.airplay_client.ui.dialogs.ConnectDialog;
import com.github.warren_bank.airplay_client.utils.NetworkUtils;
import com.github.warren_bank.airplay_client.utils.PreferencesMgr;
import com.github.warren_bank.airplay_client.utils.ToastUtils;
import com.github.warren_bank.airplay_client.utils.WakeLockMgr;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class NetworkingService extends Service implements ServiceListener, AirPlayClientCallback, ConnectDialog.Callback {
  private static final String tag          = NetworkingService.class.getSimpleName();
  private static final String ACTION_STOP  = "STOP";

  // the service type (which events to listen for)
  private static final String SERVICE_TYPE = "_airplay._tcp.local.";

  // handler
  private Handler handler;

  // send instructions to AirPlay receiver
  private AirPlayClient client;

  // global Message listener
  private MyMessageHandler myMessageHandler;

  // map of all AirPlay receivers discovered via Bonjour registration (continuously updated in background)
  private Map<String, ServiceInfo> services;

  // holder for the currently selected service
  private String selectedService;

  // local IP address
  private InetAddress localAddress;

  // JmDNS library
  private JmDNS jmdns;

  // HTTP server
  private HttpServer http;

  // screen mirroring
  private ScreenMirrorMgr mirror;

  // ---------------------------------------------------------------------------
  // Service lifecycle

  @Override
  public void onCreate() {
    super.onCreate();
    Log.d(tag, "onCreate");

    // handler
    handler = new Handler();

    // send instructions to AirPlay receiver
    client = new AirPlayClient(NetworkingService.this);

    // store global reference
    MainApp.setNetworkingService(NetworkingService.this);

    // global Message listener
    myMessageHandler = new MyMessageHandler(Looper.getMainLooper(), NetworkingService.this, client);
    MainApp.registerHandler(NetworkingService.class.getName(), myMessageHandler);

    // map of all AirPlay receivers discovered via Bonjour registration (continuously updated in background)
    services = new HashMap<String, ServiceInfo>();

    // holder for the currently selected service
    selectedService = null;

    WakeLockMgr.acquire(NetworkingService.this, WakeLockMgr.FLAG_MULTICASTLOCK);

    // JmDNS
    Thread thread = new Thread() {
      @Override
      public void run() {
        try {
          // local ip address
          localAddress = NetworkUtils.getLocalIpAddress();
          if (localAddress == null) {
            toast("Error: Unable to get local IP address");
            return;
          }

          // init jmdns
          jmdns = JmDNS.create(localAddress);
          jmdns.addServiceListener(SERVICE_TYPE, NetworkingService.this);
          toast("Using local address " + localAddress.getHostAddress());
        }
        catch (Exception e) {
          toast("Error: " + e.getMessage() == null ? "Unable to initialize discovery service" : e.getMessage());
        }
      }
    };
    thread.start();

    // http server
    http = new HttpServer();
    http.startServer(PreferencesMgr.get_server_port());

    // screen mirroring
    mirror = (Build.VERSION.SDK_INT >= 21)
      ? ScreenMirrorMgr.getInstance(getApplicationContext())
      : null;

    showNotification();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    onStart(intent, startId);
    return START_STICKY;
  }

  @Override
  public void onStart(Intent intent, int startId) {
    processIntent(intent);
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.d(tag, "onDestroy");

    shutdown();
  }

  // ---------------------------------------------------------------------------
  // private

  private void processIntent(Intent intent) {
    if (intent == null) return;

    String action = intent.getAction();
    if (action == null) return;

    switch (action) {
      case ACTION_STOP : {
        Message msg = Message.obtain();
        msg.what = Constant.Msg.Msg_Exit_Service;
        MainApp.broadcastMessage(msg);
        break;
      }
    }
  }

  private void shutdown() {
    // stop http server
    if (http != null)
      http.stopServer();

    // stop http client
    if (client != null)
      client.shutdown();

    // JmDNS
    if (jmdns != null) {
      try {
        jmdns.removeServiceListener(SERVICE_TYPE, NetworkingService.this);
        jmdns.close();
      }
      catch (Exception e) {
        toast("Error: " + e.getMessage());
      }
    }

    WakeLockMgr.release();

    MainApp.unregisterHandler(NetworkingService.class.getName());
    hideNotification();

    // remove global reference
    MainApp.setNetworkingService(null);

    // save selected service
    if (selectedService != null)
      PreferencesMgr.set_selected_service(selectedService);
  }

  // ---------------------------------------------------------------------------
  // public

  public ServiceInfo getSelectedService() {
    return services.get(selectedService);
  }

  protected InetAddress getLocalAddress() {
    if (localAddress == null)
      localAddress = NetworkUtils.getLocalIpAddress();

    return localAddress;
  }

  protected String getNetworkAddress(String defaultValue, boolean includePort) {
    getLocalAddress();

    return (localAddress == null)
      ? defaultValue
      : localAddress.getHostAddress() + (
          includePort
            ? (":" + PreferencesMgr.get_server_port())
            : ""
        );
  }

  protected void showConnectDialog(Context context) {
    new ConnectDialog(context, NetworkingService.this, services.values()).show();
  }

  protected void restartHttpServer() {
    if (http != null)
      http.stopServer();

    http = new HttpServer();
    http.startServer(PreferencesMgr.get_server_port());

    updateNotification();
  }

  protected void toast(final String message) {
    if (message == null) return;

    handler.post(new Runnable() {
      @Override
      public void run() {
        ToastUtils.showToast(NetworkingService.this, message);
      }
    });
  }

  // ---------------------------------------------------------------------------
  // jmdns.ServiceListener

  @Override
  public void serviceAdded(final ServiceEvent event) {
    toast("Found AirPlay service: " + event.getName());
    services.put(event.getInfo().getKey(), event.getInfo());
    handler.post(new Runnable() {
      @Override
      public void run() {
        jmdns.requestServiceInfo(event.getType(), event.getName(), 1000);
      }
    });
  }

  @Override
  public void serviceRemoved(ServiceEvent event) {
    toast("Removed AirPlay service: " + event.getName());
    services.remove(event.getInfo().getKey());
    if (selectedService != null && selectedService.equals(event.getName())) {
      selectedService = null;

      Message msg = Message.obtain();
      msg.what = Constant.Msg.Msg_AirPlay_Disconnect;
      MainApp.broadcastMessage(msg);
    }
  }

  @Override
  public void serviceResolved(ServiceEvent event) {
    toast("Resolved AirPlay service: " + event.getName() + " @ " + event.getInfo().getURL());
    services.put(event.getInfo().getKey(), event.getInfo());
    if (selectedService == null) {
      // try to see if the resolved one is the one that we last connected to -> autoconnect
      String remembered = PreferencesMgr.get_selected_service();
      if ((remembered != null) && remembered.equals(event.getInfo().getKey())) {
        selectedService = remembered;

        Message msg = Message.obtain();
        msg.what = Constant.Msg.Msg_AirPlay_Connect;
        msg.obj  = event.getName();
        MainApp.broadcastMessage(msg);
      }
    }
  }

  // ---------------------------------------------------------------------------
  // AirPlayClientCallback

  @Override
  public void onPutImageSuccess(File file) {
    toast("Sent image " + file.getName());
  }

  @Override
  public void onPutImageError(File file, String message) {
    toast("Error sending image " + file.getName() + (message == null ? "" : " :" + message));
  }

  @Override
  public void onPlayVideoSuccess(URL location) {
    toast("Sent video link " + location);
  }

  @Override
  public void onPlayVideoError(URL location, String message) {
    toast("Error sending video link " + location + (message == null ? "" : " :" + message));
  }

  @Override
  public void onStopVideoSuccess() {
    toast("Sent request to stop video");
  }

  @Override
  public void onStopVideoError(String message) {
    toast("Error sending request to stop video" + (message == null ? "" : " :" + message));
  }

  // ---------------------------------------------------------------------------
  // ConnectDialog.Callback

  @Override
  public void onServiceSelected(ServiceInfo serviceInfo) {
    selectedService = serviceInfo.getKey();

    Message msg = Message.obtain();
    msg.what = Constant.Msg.Msg_AirPlay_Connect;
    msg.obj  = serviceInfo.getName();
    MainApp.broadcastMessage(msg);
  }

  // ---------------------------------------------------------------------------
  // foregrounding..

  private String getNotificationChannelId() {
    return getPackageName();
  }

  private void createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= 26) {
      String channelId       = getNotificationChannelId();
      NotificationManager NM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
      NotificationChannel NC = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_HIGH);

      NC.setDescription(channelId);
      NC.setSound(null, null);
      NM.createNotificationChannel(NC);
    }
  }

  private int getNotificationId() {
    return getResources().getInteger(R.integer.NOTIFICATION_ID_NETWORKING_SERVICE);
  }

  private void showNotification() {
    if (Build.VERSION.SDK_INT < 5) {
      updateNotification();
      return;
    }

    int NOTIFICATION_ID = getNotificationId();
    Notification notification = getNotification();

    createNotificationChannel();
    startForeground(NOTIFICATION_ID, notification);
  }

  private void updateNotification() {
    int NOTIFICATION_ID = getNotificationId();
    Notification notification = getNotification();

    NotificationManager NM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    NM.notify(NOTIFICATION_ID, notification);
  }

  private void hideNotification() {
    if (Build.VERSION.SDK_INT >= 5) {
      stopForeground(true);
    }
    else {
      NotificationManager NM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
      int NOTIFICATION_ID    = getNotificationId();
      NM.cancel(NOTIFICATION_ID);
    }
  }

  private Notification getNotification() {
    Notification notification  = (Build.VERSION.SDK_INT >= 26)
      ? (new Notification.Builder(/* context= */ NetworkingService.this, /* channelId= */ getNotificationChannelId())).build()
      :  new Notification()
    ;

    notification.when          = System.currentTimeMillis();
    notification.flags         = 0;
    notification.flags        |= Notification.FLAG_ONGOING_EVENT;
    notification.flags        |= Notification.FLAG_NO_CLEAR;
    notification.icon          = R.drawable.ic_launcher;
    notification.tickerText    = getString(R.string.notification_service_ticker);
    notification.contentIntent = getPendingIntent_StopService();
    notification.deleteIntent  = getPendingIntent_StopService();

    if (Build.VERSION.SDK_INT >= 16) {
      notification.priority    = Notification.PRIORITY_HIGH;
    }
    else {
      notification.flags      |= Notification.FLAG_HIGH_PRIORITY;
    }

    if (Build.VERSION.SDK_INT >= 21) {
      notification.visibility  = Notification.VISIBILITY_PUBLIC;
    }

    RemoteViews contentView    = new RemoteViews(getPackageName(), R.layout.service_notification);
    contentView.setImageViewResource(R.id.notification_icon, R.drawable.ic_launcher);
    contentView.setTextViewText(R.id.notification_text_line1, getString(R.string.notification_service_content_line1));
    contentView.setTextViewText(R.id.notification_text_line2, getNetworkAddress("[offline]", true));
    contentView.setTextViewText(R.id.notification_text_line3, getString(R.string.notification_service_content_line3));
    notification.contentView   = contentView;

    return notification;
  }

  private PendingIntent getPendingIntent_StopService() {
    Intent intent = new Intent(NetworkingService.this, NetworkingService.class);
    intent.setAction(ACTION_STOP);

    return PendingIntent.getService(NetworkingService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
  }

}
