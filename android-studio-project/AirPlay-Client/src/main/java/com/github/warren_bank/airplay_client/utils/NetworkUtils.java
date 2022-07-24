package com.github.warren_bank.airplay_client.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class NetworkUtils {

  public static InetAddress getLocalIpAddress() {
    try {
      for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
        NetworkInterface intf = en.nextElement();

        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
          InetAddress inetAddress = enumIpAddr.nextElement();

          if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
            return inetAddress;
          }
        }
      }
    }
    catch (SocketException e) {
    }
    return null;
  }

}
