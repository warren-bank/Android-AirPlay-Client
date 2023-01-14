package com.github.warren_bank.airplay_client.utils;

import com.github.warren_bank.airplay_client.constant.Constant;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class RuntimePermissionUtils {

  // ---------------------------------------------------------------------------
  // Listener interface

  public interface RuntimePermissionListener {
    public void onRequestPermissionsGranted (int requestCode, Object passthrough);
    public void onRequestPermissionsDenied  (int requestCode, Object passthrough, String[] missingPermissions);
    public void onAllRequestsCompleted      (Exception exception, Object passthrough);
  }

  // ---------------------------------------------------------------------------
  // cache of "passthrough" Objects

  private static HashMap<Integer,Object> passthroughCache = new HashMap<Integer,Object>();

  private static void setPassthroughCache(int requestCode, Object passthrough) {
    RuntimePermissionUtils.passthroughCache.put(requestCode, passthrough);
  }

  private static Object getPassthroughCache(int requestCode) {
    Object passthrough = RuntimePermissionUtils.passthroughCache.remove(requestCode);
    return passthrough;
  }

  // ---------------------------------------------------------------------------
  // convenience class: request multiple runtime permissions in sequence

  private static final class SequentialRequesterClass implements RuntimePermissionListener {

    // ---------------------------------
    // static

    public static SequentialRequesterClass instance = null;

    public static List<Integer> convertRequestCodes(int[] requestCodes) throws Exception {
      if (requestCodes == null)
        throw new Exception("parameter cannot be null: requestCodes");
      if (requestCodes.length == 0)
        throw new Exception("array parameter cannot be empty: requestCodes");

      List<Integer> result = new ArrayList<Integer>(requestCodes.length);
      for (int requestCode : requestCodes) {
        result.add(Integer.valueOf(requestCode));
      }
      return result;
    }

    // ---------------------------------
    // instance

    private Activity mActivity;
    private RuntimePermissionListener mListener;
    private List<Integer> mRequestCodes;
    private Object mPassthrough;
    private int mCurrentRequestCode;

    public SequentialRequesterClass(Activity activity, RuntimePermissionListener listener, int[] requestCodes, Object passthrough) throws Exception {
      this(activity, listener, SequentialRequesterClass.convertRequestCodes(requestCodes), passthrough);
    }

    public SequentialRequesterClass(Activity activity, RuntimePermissionListener listener, List<Integer> requestCodes, Object passthrough) throws Exception {
      if (requestCodes.isEmpty())
        throw new Exception("list parameter cannot be empty: requestCodes");

      if (SequentialRequesterClass.instance != null) {
        SequentialRequesterClass.instance.appendRequestCodes(requestCodes);
        return;
      }

      if (activity == null)
        throw new Exception("parameter cannot be null: activity");
      if (listener == null)
        throw new Exception("parameter cannot be null: listener");

      mActivity           = activity;
      mListener           = listener;
      mRequestCodes       = requestCodes;
      mPassthrough        = passthrough;
      mCurrentRequestCode = -1;

      SequentialRequesterClass.instance = this;

      requestNextCode();
    }

    public void appendRequestCodes(List<Integer> newRequestCodes) {
      if ((newRequestCodes != null) && !newRequestCodes.isEmpty())
        mRequestCodes.addAll(newRequestCodes);
    }

    public boolean isSameListener(RuntimePermissionListener listener) {
      return (mListener == listener);
    }

    @Override
    public void onRequestPermissionsGranted(int requestCode, Object passthrough) {
      mListener.onRequestPermissionsGranted(requestCode, passthrough);

      if (mCurrentRequestCode == requestCode)
        requestNextCode();
    }

    @Override
    public void onRequestPermissionsDenied(int requestCode, Object passthrough, String[] missingPermissions) {
      mListener.onRequestPermissionsDenied(requestCode, passthrough, missingPermissions);

      if (mCurrentRequestCode == requestCode)
        requestNextCode();
    }

    @Override
    public void onAllRequestsCompleted(Exception exception, Object passthrough) {
      mListener.onAllRequestsCompleted(exception, mPassthrough);
      SequentialRequesterClass.instance = null;
    }

    private void requestNextCode() {
      if (mRequestCodes.isEmpty()) {
        this.onAllRequestsCompleted((Exception) null, (Object) null);
      }
      else {
        mCurrentRequestCode = ((Integer) mRequestCodes.remove(0)).intValue();
        RuntimePermissionUtils.requestPermissions(mActivity, /* listener= */ this, mCurrentRequestCode);
      }
    }
  }

  // ---------------------------------------------------------------------------
  // public API

  public static boolean hasAllPermissions(Context context, int requestCode) {
    String[] allRequestedPermissions = RuntimePermissionUtils.getAllRequestedPermissions(requestCode);
    return RuntimePermissionUtils.hasAllPermissions(context, allRequestedPermissions);
  }

  public static boolean hasAllPermissions(Context context, String[] allRequestedPermissions) {
    String[] missingPermissions = RuntimePermissionUtils.getMissingPermissions(context, allRequestedPermissions);
    return (missingPermissions == null);
  }

  public static void requestPermissions(Activity activity, RuntimePermissionListener listener, int requestCode) {
    String[] allRequestedPermissions = RuntimePermissionUtils.getAllRequestedPermissions(requestCode);
    RuntimePermissionUtils.requestPermissions(activity, listener, requestCode, allRequestedPermissions);
  }

  public static void requestPermissions(Activity activity, RuntimePermissionListener listener, int requestCode, String[] allRequestedPermissions) {
    Object passthrough = null;
    RuntimePermissionUtils.requestPermissions(activity, listener, requestCode, allRequestedPermissions, passthrough);
  }

  public static void requestPermissions(Activity activity, RuntimePermissionListener listener, int requestCode, String[] allRequestedPermissions, Object passthrough) {
    String[] missingPermissions = RuntimePermissionUtils.getMissingPermissions(activity, allRequestedPermissions);

    if ((SequentialRequesterClass.instance != null) && SequentialRequesterClass.instance.isSameListener(listener))
      listener = (RuntimePermissionListener) SequentialRequesterClass.instance;

    if (missingPermissions == null) {
      listener.onRequestPermissionsGranted(requestCode, passthrough);
    }
    else {
      RuntimePermissionUtils.setPassthroughCache(requestCode, passthrough);

      activity.requestPermissions(missingPermissions, requestCode);
    }
  }

  public static void requestAllPermissions(Activity activity, RuntimePermissionListener listener, int[] requestCodes) {
    Object passthrough = null;
    RuntimePermissionUtils.requestAllPermissions(activity, listener, requestCodes, passthrough);
  }

  public static void requestAllPermissions(Activity activity, RuntimePermissionListener listener, int[] requestCodes, Object passthrough) {
    try {
      new RuntimePermissionUtils.SequentialRequesterClass(activity, listener, requestCodes, passthrough);
    }
    catch(Exception exception) {
      listener.onAllRequestsCompleted(exception, passthrough);
    }
  }

  public static void onRequestPermissionsResult(RuntimePermissionListener listener, int requestCode, String[] permissions, int[] grantResults) {
    Object passthrough          = RuntimePermissionUtils.getPassthroughCache(requestCode);
    String[] missingPermissions = RuntimePermissionUtils.getMissingPermissions(permissions, grantResults);

    if ((SequentialRequesterClass.instance != null) && SequentialRequesterClass.instance.isSameListener(listener))
      listener = (RuntimePermissionListener) SequentialRequesterClass.instance;

    if (missingPermissions == null) {
      listener.onRequestPermissionsGranted(requestCode, passthrough);
    }
    else {
      listener.onRequestPermissionsDenied(requestCode, passthrough, missingPermissions);
    }
  }

  public static void onActivityResult(RuntimePermissionListener listener, int requestCode, int resultCode, Intent data) {
    Object passthrough = RuntimePermissionUtils.getPassthroughCache(requestCode);

    if ((SequentialRequesterClass.instance != null) && SequentialRequesterClass.instance.isSameListener(listener))
      listener = (RuntimePermissionListener) SequentialRequesterClass.instance;

    if (resultCode == Activity.RESULT_OK) {
      listener.onRequestPermissionsGranted(requestCode, passthrough);
    }
    else {
      listener.onRequestPermissionsDenied(requestCode, passthrough, /* missingPermissions= */ null);
    }
  }

  // ---------------------------------------------------------------------------
  // internal

  private static String[] getAllRequestedPermissions(int requestCode) {
    String[] allRequestedPermissions = null;

    switch(requestCode) {
      case Constant.PermissionRequestCode.POST_NOTIFICATIONS : {
        if (Build.VERSION.SDK_INT >= 33) {
          allRequestedPermissions = new String[]{"android.permission.POST_NOTIFICATIONS"};
        }
        break;
      }
      case Constant.PermissionRequestCode.READ_EXTERNAL_STORAGE : {
        allRequestedPermissions = (Build.VERSION.SDK_INT >= 33)
          ? new String[]{"android.permission.READ_MEDIA_AUDIO", "android.permission.READ_MEDIA_IMAGES", "android.permission.READ_MEDIA_VIDEO"}
          : new String[]{"android.permission.READ_EXTERNAL_STORAGE"};
        break;
      }
    }

    return allRequestedPermissions;
  }

  private static String[] getMissingPermissions(Context context, int requestCode) {
    String[] allRequestedPermissions = RuntimePermissionUtils.getAllRequestedPermissions(requestCode);
    return RuntimePermissionUtils.getMissingPermissions(context, allRequestedPermissions);
  }

  private static String[] getMissingPermissions(Context context, String[] allRequestedPermissions) {
    if (Build.VERSION.SDK_INT < 23)
      return null;

    if ((allRequestedPermissions == null) || (allRequestedPermissions.length == 0))
      return null;

    List<String> missingPermissions = new ArrayList<String>();

    for (String permission : allRequestedPermissions) {
      if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
        missingPermissions.add(permission);
      }
    }

    if (missingPermissions.isEmpty())
      return null;

    return missingPermissions.toArray(new String[missingPermissions.size()]);
  }

  private static String[] getMissingPermissions(String[] allRequestedPermissions, int[] allGrantResults) {
    if ((allRequestedPermissions == null) || (allRequestedPermissions.length == 0))
      return null;

    if ((allGrantResults == null) || (allGrantResults.length == 0))
      return allRequestedPermissions;

    List<String> missingPermissions = new ArrayList<String>();
    int index;

    for (index = 0; (index < allGrantResults.length) && (index < allRequestedPermissions.length); index++) {
      if (allGrantResults[index] != PackageManager.PERMISSION_GRANTED) {
        missingPermissions.add(allRequestedPermissions[index]);
      }
    }

    while (index < allRequestedPermissions.length) {
      missingPermissions.add(allRequestedPermissions[index]);
      index++;
    }

    if (missingPermissions.isEmpty())
      return null;

    return missingPermissions.toArray(new String[missingPermissions.size()]);
  }

  // ---------------------------------------------------------------------------
  // special case: "android.permission.MANAGE_EXTERNAL_STORAGE"

  public static void showFilePermissions(Activity activity) {
    int requestCode = Constant.PermissionRequestCode.MANAGE_EXTERNAL_STORAGE;
    RuntimePermissionUtils.showFilePermissions(activity, requestCode);
  }

  public static void showFilePermissions(Activity activity, int requestCode) {
    Object passthrough = null;
    RuntimePermissionUtils.showFilePermissions(activity, requestCode, passthrough);
  }

  public static void showFilePermissions(Activity activity, int requestCode, Object passthrough) {
    Uri uri       = Uri.parse("package:" + activity.getPackageName());
    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);

    RuntimePermissionUtils.setPassthroughCache(requestCode, passthrough);

    activity.startActivityForResult(intent, requestCode);
  }

  public static boolean hasFilePermissions() {
    return RuntimePermissionUtils.canAccessAllFiles();
  }

  private static boolean canAccessAllFiles() {
    return (Build.VERSION.SDK_INT < 30)
      ? true
      : Environment.isExternalStorageManager();
  }

  // ---------------------------------------------------------------------------
}
