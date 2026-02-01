package com.github.warren_bank.airplay_client.ui;

import com.github.warren_bank.airplay_client.R;
import com.github.warren_bank.airplay_client.utils.PreferencesMgr;
import com.github.warren_bank.airplay_client.utils.ToastUtils;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;

public final class SettingsActivity extends PreferenceActivity {
  private static final int MIN_PORT_NUMBER = 1025;
  private static final int MAX_PORT_NUMBER = 65534;

  public static Intent getStartIntent(Context context) {
    return new Intent(context, SettingsActivity.class);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // action bar icon as home link
    ActionBar actionBar = getActionBar();
    actionBar.setSubtitle(
      getString(R.string.settings_activity_subtitle)
    );
    actionBar.setHomeButtonEnabled(true);
    actionBar.setDisplayUseLogoEnabled(false);
    actionBar.setDisplayHomeAsUpEnabled(true);

    getFragmentManager().beginTransaction().replace(android.R.id.content, new ScreenStreamPreferenceFragment()).commit();
  }

  public static class ScreenStreamPreferenceFragment extends PreferenceFragment {
    int mResizeFactor;
    int mIndex;

    private Handler handler = new Handler();

    private void toast(final String message) {
      if (message == null) return;

      handler.post(new Runnable() {
        @Override
        public void run() {
          ToastUtils.showToast(getActivity(), message);
        }
      });
    }

    private CharSequence normalizeListName(CharSequence name) {
      return name.toString().replace(" [Default]", "");
    }

    private void setResizeFactor(final int resizeFactor, final TextView resizeFactorText) {
      mResizeFactor = resizeFactor;
      resizeFactorText.setText(String.format(Locale.US, "%.1fx", mResizeFactor / 10f));
      if (mResizeFactor > 10) {
        int color = getResources().getInteger(R.color.colorAccent);
        resizeFactorText.setTextColor(color);
        resizeFactorText.setTypeface(resizeFactorText.getTypeface(), Typeface.BOLD);
      } else {
        int color = getResources().getInteger(R.color.textColorSecondary);
        resizeFactorText.setTextColor(color);
        resizeFactorText.setTypeface(Typeface.DEFAULT);
      }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.preferences);

      // ===============================
      // Graphical User Interface
      // ===============================

      final ListPreference folderLayoutPreference = (ListPreference) findPreference(getString(R.string.pref_key_folder_layout));
      mIndex = folderLayoutPreference.findIndexOfValue(folderLayoutPreference.getValue());
      folderLayoutPreference.setSummary(
          getString(R.string.pref_summary_folder_layout)
        + getString(R.string.settings_activity_value)
        + normalizeListName(folderLayoutPreference.getEntries()[mIndex])
      );
      folderLayoutPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object data) {
          final int index = folderLayoutPreference.findIndexOfValue(data.toString());
          folderLayoutPreference.setSummary(
              getString(R.string.pref_summary_folder_layout)
            + getString(R.string.settings_activity_value)
            + normalizeListName(folderLayoutPreference.getEntries()[index])
          );
          return true;
        }
      });

      // ===============================
      // Streaming Media
      // ===============================

      final String portRange = String.format(getString(R.string.settings_activity_port_range), MIN_PORT_NUMBER, MAX_PORT_NUMBER);
      final EditTextPreference serverPortTextPreference = (EditTextPreference) findPreference(getString(R.string.pref_key_server_port));
      serverPortTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object data) {
          final String portString = data.toString();
          if (portString == null || portString.length() == 0 || portString.length() > 5 || portString.length() < 4) {
            toast(portRange);
            return false;
          }
          final int portNumber = Integer.parseInt(portString);
          if ((portNumber < MIN_PORT_NUMBER) || (portNumber > MAX_PORT_NUMBER)) {
            toast(portRange);
            return false;
          }
          return true;
        }
      });

      // ===============================
      // Images
      // ===============================

      final ListPreference imageTransitionPreference = (ListPreference) findPreference(getString(R.string.pref_key_image_transition));
      mIndex = imageTransitionPreference.findIndexOfValue(imageTransitionPreference.getValue());
      imageTransitionPreference.setSummary(
          getString(R.string.pref_summary_image_transition)
        + getString(R.string.settings_activity_value)
        + normalizeListName(imageTransitionPreference.getEntries()[mIndex])
      );
      imageTransitionPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object data) {
          final int index = imageTransitionPreference.findIndexOfValue(data.toString());
          imageTransitionPreference.setSummary(
              getString(R.string.pref_summary_image_transition)
            + getString(R.string.settings_activity_value)
            + normalizeListName(imageTransitionPreference.getEntries()[index])
          );
          return true;
        }
      });

      // ===============================
      // Screen Mirroring
      // ===============================

      final Preference resizePreference = findPreference(getString(R.string.pref_key_resize_factor));
      resizePreference.setSummary(
          getString(R.string.pref_summary_resize_factor)
        + getString(R.string.settings_activity_value)
        + String.format(Locale.US, "%.1fx", PreferencesMgr.get_resize_factor() / 10f)
      );
      resizePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(final Preference preference) {
          final LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
          final View resizeView = layoutInflater.inflate(R.layout.pref_resize, null);
          final TextView resizeFactor = (TextView) resizeView.findViewById(R.id.pref_resize_dialog_textView);
          setResizeFactor(PreferencesMgr.get_resize_factor(), resizeFactor);

          final SeekBar seekBar = (SeekBar) resizeView.findViewById(R.id.pref_resize_dialog_seekBar);
          seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
              setResizeFactor(progress + 1, resizeFactor);
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {}
          });
          seekBar.setProgress(mResizeFactor - 1);

          new AlertDialog.Builder(getActivity())
            .setView(resizeView)
            .setCancelable(true)
            .setIcon(R.drawable.ic_pref_resize_black_24dp)
            .setTitle(R.string.pref_title_resize_factor)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(final DialogInterface dialog, final int which) {
                PreferencesMgr.set_resize_factor(mResizeFactor);
                resizePreference.setSummary(
                    getString(R.string.pref_summary_resize_factor)
                  + getString(R.string.settings_activity_value)
                  + String.format(Locale.US, "%.1fx", PreferencesMgr.get_resize_factor() / 10f)
                );
                dialog.dismiss();
              }
            })
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(final DialogInterface dialog, final int which) {
                dialog.cancel();
              }
            }).create().show();

          return true;
        }
      });

      final ListPreference jpegQualityPreference = (ListPreference) findPreference(getString(R.string.pref_key_jpeg_quality));
      mIndex = jpegQualityPreference.findIndexOfValue(jpegQualityPreference.getValue());
      jpegQualityPreference.setSummary(
          getString(R.string.pref_summary_jpeg_quality)
        + getString(R.string.settings_activity_value)
        + normalizeListName(jpegQualityPreference.getEntries()[mIndex])
      );
      jpegQualityPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object data) {
          final int index = jpegQualityPreference.findIndexOfValue(data.toString());
          jpegQualityPreference.setSummary(
              getString(R.string.pref_summary_jpeg_quality)
            + getString(R.string.settings_activity_value)
            + normalizeListName(jpegQualityPreference.getEntries()[index])
          );
          return true;
        }
      });

    }
  }
}
