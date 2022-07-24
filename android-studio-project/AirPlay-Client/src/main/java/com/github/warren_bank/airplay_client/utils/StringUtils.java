package com.github.warren_bank.airplay_client.utils;

public class StringUtils {

  // unlike TextUtils, trim leading/trailing whitespace before testing for 0-length
  public static boolean isEmpty(String text) {
    return (text == null) || text.trim().isEmpty();
  }

  public static String normalizeBooleanString(String bool) {
    if (bool != null)
      bool = bool.toLowerCase().trim();

    return (
        StringUtils.isEmpty(bool)
     || bool.equals("false")
     || bool.equals("0")
     || bool.equals("null")
     || bool.equals("undefined")
    ) ? "false" : "true";
  }

}
