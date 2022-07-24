package com.github.warren_bank.airplay_client.ui;

import com.github.warren_bank.airplay_client.R;
import com.github.warren_bank.airplay_client.utils.AirPlayUtils;
import com.github.warren_bank.airplay_client.utils.PreferencesMgr;
import com.github.warren_bank.airplay_client.utils.ToastUtils;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * The settings activity.
 *
 * @author Tuomas Tikka
 */
public class SettingsActivity extends Activity {
  // server port
  EditText serverPort;

  // image transition
  Spinner imageTransition;

  // handler
  private Handler handler = new Handler();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.settings);

    // action bar icon as home link
    ActionBar actionBar = getActionBar();
    actionBar.setSubtitle("Settings");
    actionBar.setHomeButtonEnabled(true);
    actionBar.setDisplayUseLogoEnabled(false);
    actionBar.setDisplayHomeAsUpEnabled(true);

    // server port
    serverPort = (EditText) findViewById(R.id.server_port);

    // image transition
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.image_transition_item, R.id.transition, AirPlayUtils.getTransitionDescriptions());
    imageTransition = (Spinner) findViewById(R.id.image_transition);
    imageTransition.setAdapter(adapter);

    // load settings
    loadSettings();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.settings_actions, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.save: {
        if (saveSettings()) {
          toast("Settings Saved");
          finish();
        }
        break;
      }
      case R.id.cancel:
      case android.R.id.home: {
        finish();
        break;
      }
      default:
        break;
    }
    return true;
  }

  private void loadSettings() {
    // server port
    serverPort.setText("" + PreferencesMgr.get_server_port());

    // image transition
    imageTransition.setSelection(PreferencesMgr.get_image_transition());
  }

  private boolean saveSettings() {
    // validate port number
    try {
      int i = Integer.parseInt(serverPort.getText().toString());
      if ((i < 1024) || (i > 65535)) {
        toast("Server port: must be in range 1024-65535");
        return false;
      }
    }
    catch (Exception e) {
      toast("Server port: must be a number");
      return false;
    }

    PreferencesMgr.set_server_port(Integer.valueOf(serverPort.getText().toString()));
    PreferencesMgr.set_image_transition(Integer.valueOf(imageTransition.getSelectedItemPosition()));
    return true;
  }

  private void toast(final String message) {
    if (message == null) return;

    handler.post(new Runnable() {
      @Override
      public void run() {
        ToastUtils.showToast(SettingsActivity.this, message);
      }
    });
  }

}
