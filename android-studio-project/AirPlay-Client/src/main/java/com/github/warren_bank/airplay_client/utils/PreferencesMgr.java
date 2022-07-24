package com.github.warren_bank.airplay_client.utils;

import com.github.warren_bank.airplay_client.MainApp;
import com.github.warren_bank.airplay_client.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.util.ArrayList;

public class PreferencesMgr {

  // ---------------------------------------------------------------------------
  // internal:

  private static Context getApplicationContext() {
    return (Context) MainApp.getInstance();
  }

  private static SharedPreferences getPrefs(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context);
  }

  private static SharedPreferences.Editor getPrefsEditor(Context context) {
    SharedPreferences prefs = getPrefs(context);

    return getPrefsEditor(prefs);
  }

  private static SharedPreferences.Editor getPrefsEditor(SharedPreferences prefs) {
    return prefs.edit();
  }

  // ---------------------------------------------------------------------------
  // internal generic getters:

  private static String getPrefString(int pref_key_id, String default_value) {
    Context context         = getApplicationContext();
    SharedPreferences prefs = getPrefs(context);

    return getPrefString(context, prefs, pref_key_id, default_value);
  }

  private static String getPrefString(Context context, SharedPreferences prefs, int pref_key_id, String default_value) {
    String pref_key         = context.getString(pref_key_id);

    return prefs.getString(pref_key, default_value);
  }

  // -----------------------------------

  private static boolean getPrefBoolean(int pref_key_id, boolean default_value) {
    Context context         = getApplicationContext();
    SharedPreferences prefs = getPrefs(context);

    return getPrefBoolean(context, prefs, pref_key_id, default_value);
  }

  private static boolean getPrefBoolean(Context context, SharedPreferences prefs, int pref_key_id, boolean default_value) {
    String pref_key         = context.getString(pref_key_id);

    return prefs.getBoolean(pref_key, default_value);
  }

  // -----------------------------------

  private static int getPrefInteger(int pref_key_id, int default_value) {
    Context context         = getApplicationContext();
    SharedPreferences prefs = getPrefs(context);

    return getPrefInteger(context, prefs, pref_key_id, default_value);
  }

  private static int getPrefInteger(Context context, SharedPreferences prefs, int pref_key_id, int default_value) {
    String pref_key         = context.getString(pref_key_id);

    return prefs.getInt(pref_key, default_value);
  }

  // -----------------------------------

  private static float getPrefFloat(int pref_key_id, float default_value) {
    Context context         = getApplicationContext();
    SharedPreferences prefs = getPrefs(context);

    return getPrefFloat(context, prefs, pref_key_id, default_value);
  }

  private static float getPrefFloat(Context context, SharedPreferences prefs, int pref_key_id, float default_value) {
    String pref_key         = context.getString(pref_key_id);

    return prefs.getFloat(pref_key, default_value);
  }

  // ---------------------------------------------------------------------------
  // internal getters:

  private static String get_selected_service(Context context, SharedPreferences prefs) {
    String default_value = null;

    return ((context == null) || (prefs == null))
      ? getPrefString(
          /* pref_key_id= */   R.string.prefkey_selected_service,
          default_value
        )
      : getPrefString(
          context,
          prefs,
          /* pref_key_id= */   R.string.prefkey_selected_service,
          default_value
        )
    ;
  }

  private static String get_selected_folder(Context context, SharedPreferences prefs) {
    String default_value = Environment.getExternalStorageDirectory().getAbsolutePath();

    return ((context == null) || (prefs == null))
      ? getPrefString(
          /* pref_key_id= */   R.string.prefkey_selected_folder,
          default_value
        )
      : getPrefString(
          context,
          prefs,
          /* pref_key_id= */   R.string.prefkey_selected_folder,
          default_value
        )
    ;
  }

  private static int get_image_transition(Context context, SharedPreferences prefs) {
    int default_value = 0;

    return ((context == null) || (prefs == null))
      ? getPrefInteger(
          /* pref_key_id= */   R.string.prefkey_image_transition,
          default_value
        )
      : getPrefInteger(
          context,
          prefs,
          /* pref_key_id= */   R.string.prefkey_image_transition,
          default_value
        )
    ;
  }

  private static int get_server_port(Context context, SharedPreferences prefs) {
    int default_value = context.getResources().getInteger(R.integer.prefval_server_port);

    return ((context == null) || (prefs == null))
      ? getPrefInteger(
          /* pref_key_id= */   R.string.prefkey_server_port,
          default_value
        )
      : getPrefInteger(
          context,
          prefs,
          /* pref_key_id= */   R.string.prefkey_server_port,
          default_value
        )
    ;
  }

  // ---------------------------------------------------------------------------
  // internal state:

  private static boolean is_initialized = false;

  private static String  selected_service;
  private static String  selected_folder;
  private static int     image_transition;
  private static int     server_port;

  private static void initialize() {
    if (is_initialized) return;

    is_initialized          = true;
    Context context         = getApplicationContext();
    SharedPreferences prefs = getPrefs(context);

    selected_service        = get_selected_service(context, prefs);
    selected_folder         = get_selected_folder(context, prefs);
    image_transition        = get_image_transition(context, prefs);
    server_port             = get_server_port(context, prefs);
  }

  // ---------------------------------------------------------------------------
  // public getters:

  public static String get_selected_service() {
    initialize();
    return selected_service;
  }

  public static String get_selected_folder() {
    initialize();
    return selected_folder;
  }

  public static int get_image_transition() {
    initialize();
    return image_transition;
  }

  public static int get_server_port() {
    initialize();
    return server_port;
  }

  // ---------------------------------------------------------------------------
  // internal generic setters:

  private static boolean setPrefString(Context context, SharedPreferences.Editor editor, int pref_key_id, String old_value, String new_value) {
    new_value = StringUtils.isEmpty(new_value) ? null : new_value.trim();

    boolean did_edit = false;

    if (
      ((old_value != null) && !old_value.equals(new_value)) ||
      ((old_value == null) && (new_value != null))
    ) {
      did_edit = true;

      if (new_value == null) {
        editor.remove(
          context.getString(pref_key_id)
        );
      }
      else {
        editor.putString(
          context.getString(pref_key_id),
          new_value
        );
      }
    }

    return did_edit;
  }

  // -----------------------------------

  private static boolean setPrefBoolean(Context context, SharedPreferences.Editor editor, int pref_key_id, boolean old_value, Boolean new_value) {
    boolean did_edit  = false;

    if ((new_value == null) || (new_value.booleanValue() != old_value)) {
      did_edit = true;

      if (new_value == null) {
        editor.remove(
          context.getString(pref_key_id)
        );
      }
      else {
        editor.putBoolean(
          context.getString(pref_key_id),
          new_value.booleanValue()
        );
      }
    }

    return did_edit;
  }

  // -----------------------------------

  private static boolean setPrefInteger(Context context, SharedPreferences.Editor editor, int pref_key_id, int old_value, Integer new_value) {
    boolean did_edit = false;

    if ((new_value == null) || (new_value.intValue() != old_value)) {
      did_edit = true;

      if (new_value == null) {
        editor.remove(
          context.getString(pref_key_id)
        );
      }
      else {
        editor.putInt(
          context.getString(pref_key_id),
          new_value.intValue()
        );
      }
    }

    return did_edit;
  }

  // -----------------------------------

  private static boolean setPrefFloat(Context context, SharedPreferences.Editor editor, int pref_key_id, float old_value, Float new_value) {
    boolean did_edit = false;

    if ((new_value == null) || (new_value.floatValue() != old_value)) {
      did_edit = true;

      if (new_value == null) {
        editor.remove(
          context.getString(pref_key_id)
        );
      }
      else {
        editor.putFloat(
          context.getString(pref_key_id),
          new_value.floatValue()
        );
      }
    }

    return did_edit;
  }

  // ---------------------------------------------------------------------------
  // listeners:

  public interface OnPreferenceChangeListener {
    void onPreferenceChange(int pref_key_id);
  }

  private static ArrayList<OnPreferenceChangeListener> listeners = new ArrayList<OnPreferenceChangeListener>();

  public static void addOnPreferenceChangedListener(OnPreferenceChangeListener listener) {
    listeners.add(listener);
  }

  public static void removeOnPreferenceChangedListener(OnPreferenceChangeListener listener) {
    listeners.remove(listener);
  }

  private static void notifyListeners(int pref_key_id) {
    for (OnPreferenceChangeListener listener : listeners) {
      listener.onPreferenceChange(pref_key_id);
    }
  }

  // ---------------------------------------------------------------------------
  // public setters:

  public static boolean set_selected_service(String new_value) {
    initialize();

    Context context                 = getApplicationContext();
    SharedPreferences prefs         = getPrefs(context);
    SharedPreferences.Editor editor = getPrefsEditor(prefs);
    int pref_key_id                 = R.string.prefkey_selected_service;
    boolean did_edit;

    did_edit = setPrefString(context, editor, pref_key_id, /* old_value= */ selected_service, new_value);

    if (did_edit) {
      did_edit = editor.commit();
    }

    if (did_edit) {
      selected_service = get_selected_service(context, prefs);
      notifyListeners(pref_key_id);
    }

    return did_edit;
  }

  public static boolean set_selected_folder(String new_value) {
    initialize();

    Context context                 = getApplicationContext();
    SharedPreferences prefs         = getPrefs(context);
    SharedPreferences.Editor editor = getPrefsEditor(prefs);
    int pref_key_id                 = R.string.prefkey_selected_folder;
    boolean did_edit;

    did_edit = setPrefString(context, editor, pref_key_id, /* old_value= */ selected_folder, new_value);

    if (did_edit) {
      did_edit = editor.commit();
    }

    if (did_edit) {
      selected_folder = get_selected_folder(context, prefs);
      notifyListeners(pref_key_id);
    }

    return did_edit;
  }

  public static boolean set_image_transition(Integer new_value) {
    initialize();

    Context context                 = getApplicationContext();
    SharedPreferences prefs         = getPrefs(context);
    SharedPreferences.Editor editor = getPrefsEditor(prefs);
    int pref_key_id                 = R.string.prefkey_image_transition;
    boolean did_edit;

    did_edit = setPrefInteger(context, editor, pref_key_id, /* old_value= */ image_transition, new_value);

    if (did_edit) {
      did_edit = editor.commit();
    }

    if (did_edit) {
      image_transition = get_image_transition(context, prefs);
      notifyListeners(pref_key_id);
    }

    return did_edit;
  }

  public static boolean set_server_port(Integer new_value) {
    initialize();

    Context context                 = getApplicationContext();
    SharedPreferences prefs         = getPrefs(context);
    SharedPreferences.Editor editor = getPrefsEditor(prefs);
    int pref_key_id                 = R.string.prefkey_server_port;
    boolean did_edit;

    did_edit = setPrefInteger(context, editor, pref_key_id, /* old_value= */ server_port, new_value);

    if (did_edit) {
      did_edit = editor.commit();
    }

    if (did_edit) {
      server_port = get_server_port(context, prefs);
      notifyListeners(pref_key_id);
    }

    return did_edit;
  }

}
