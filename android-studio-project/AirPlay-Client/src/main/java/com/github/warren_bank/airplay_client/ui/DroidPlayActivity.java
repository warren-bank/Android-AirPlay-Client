package com.github.warren_bank.airplay_client.ui;

import com.github.warren_bank.airplay_client.MainApp;
import com.github.warren_bank.airplay_client.R;
import com.github.warren_bank.airplay_client.constant.Constant;
import com.github.warren_bank.airplay_client.service.NetworkingService;
import com.github.warren_bank.airplay_client.ui.adapters.ImageAdapter;
import com.github.warren_bank.airplay_client.ui.adapters.NavigationAdapter;
import com.github.warren_bank.airplay_client.ui.adapters.NavigationItem;
import com.github.warren_bank.airplay_client.ui.dialogs.FolderDialog;
import com.github.warren_bank.airplay_client.utils.ExternalStorageUtils;
import com.github.warren_bank.airplay_client.utils.PreferencesMgr;
import com.github.warren_bank.airplay_client.utils.ToastUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.drawerlayout.widget.DrawerLayout;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class DroidPlayActivity extends Activity implements AdapterView.OnItemClickListener, FolderDialog.Callback {
  private Handler handler;

  // holder for the navigation "drawer" layout
  private DrawerLayout navigationLayout;

  // holder for the navigation "drawer" adapter
  private NavigationAdapter navigationAdapter;

  // holder for the navigation "drawer" list
  private ListView navigationList;

  // the custom adapter for the thumbnail images
  private ImageAdapter adapter;

  // ---------------------------------------------------------------------------
  // Activity lifecycle

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    handler = new DroidPlayHandler(DroidPlayActivity.this);
    MainApp.registerHandler(DroidPlayActivity.class.getName(), handler);

    setContentView(R.layout.main);

    // action bar
    subtitle("Not connected");

    // navigation drawer
    navigationLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    List<NavigationItem> navigationItems = new ArrayList<NavigationItem>();
    navigationItems.add(new NavigationItem("connect",   "Connect to AirPlay...", R.drawable.ic_cast_connected_grey600_36dp));
    navigationItems.add(new NavigationItem("stop",      "Stop playback",         R.drawable.ic_stop_grey600_36dp          ));
    navigationAdapter = new NavigationAdapter(DroidPlayActivity.this, navigationItems);
    navigationList = (ListView) findViewById(R.id.drawer);
    navigationList.setAdapter(navigationAdapter);
    navigationList.setOnItemClickListener(DroidPlayActivity.this);

    if (ExternalStorageUtils.has_permission(DroidPlayActivity.this))
      onPermissionGranted();
    else
      ExternalStorageUtils.request_permission(DroidPlayActivity.this);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (ExternalStorageUtils.is_permission_granted(DroidPlayActivity.this, requestCode, grantResults))
      onPermissionGranted();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main_actions, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.settings : {
        Intent intent = new Intent(DroidPlayActivity.this, SettingsActivity.class);
        startActivity(intent);
        break;
      }
      case R.id.exit : {
        Message msg = Message.obtain();
        msg.what = Constant.Msg.Msg_Exit_Service;
        MainApp.broadcastMessage(msg);
        break;
      }
    }
    return true;
  }

  @Override
  protected void onDestroy() {
    MainApp.unregisterHandler(DroidPlayActivity.class.getName());

    // save selected folder
    if ((adapter != null) && (adapter.getFolder() != null))
      PreferencesMgr.set_selected_folder(adapter.getFolder().getAbsolutePath());

    super.onDestroy();
  }

  // ---------------------------------------------------------------------------
  // FolderDialog.Callback

  @Override
  public void onFolderSelected(File folder) {
    adapter.setFolder(folder);
    updateFolder(folder.getAbsolutePath());
  }

  // ---------------------------------------------------------------------------
  // AdapterView.OnItemClickListener

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    NavigationItem item = navigationAdapter.getItem(position);
    if (item == null) return;

    switch(item.getTag()) {
      case "connect": {
        Message msg = Message.obtain();
        msg.what = Constant.Msg.Msg_AirPlay_Show_Connect_Dialog;
        msg.obj  = (Context) DroidPlayActivity.this;
        MainApp.broadcastMessage(msg);
        break;
      }
      case "pictures": {
        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        adapter.setFolder(folder);
        updateFolder(folder.getAbsolutePath());
        break;
      }
      case "videos": {
        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        adapter.setFolder(folder);
        updateFolder(folder.getAbsolutePath());
        break;
      }
      case "downloads": {
        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        adapter.setFolder(folder);
        updateFolder(folder.getAbsolutePath());
        break;
      }
      case "folders": {
        new FolderDialog(DroidPlayActivity.this, DroidPlayActivity.this, adapter.getFolder()).show();
        break;
      }
      case "stop": {
        Message msg = Message.obtain();
        msg.what = Constant.Msg.Msg_Stop;
        MainApp.broadcastMessage(msg);
        break;
      }
    }

    navigationLayout.closeDrawer(navigationList);
  }

  // ---------------------------------------------------------------------------
  // private

  private void startNetworkingService() {
    Intent intent = new Intent(getApplicationContext(), NetworkingService.class);
    MainApp.getInstance().startService(intent);
  }

  private void onPermissionGranted() {
    // update navigation drawer with additional options
    navigationAdapter.clear();
    navigationAdapter.add(new NavigationItem("connect",   "Connect to AirPlay...", R.drawable.ic_cast_connected_grey600_36dp));
    navigationAdapter.add(new NavigationItem("pictures",  "Pictures",              R.drawable.ic_image_grey600_36dp         ));
    navigationAdapter.add(new NavigationItem("videos",    "Videos",                R.drawable.ic_videocam_grey600_36dp      ));
    navigationAdapter.add(new NavigationItem("downloads", "Downloads",             R.drawable.ic_file_download_grey600_36dp ));
    navigationAdapter.add(new NavigationItem("folders",   "Choose folder...",      R.drawable.ic_folder_grey600_36dp        ));
    navigationAdapter.add(new NavigationItem("stop",      "Stop playback",         R.drawable.ic_stop_grey600_36dp          ));
    navigationAdapter.notifyDataSetChanged();

    // load selected folder
    File folder = new File(PreferencesMgr.get_selected_folder());

    // update folder label
    updateFolder(folder.getAbsolutePath());

    adapter = new ImageAdapter(DroidPlayActivity.this, folder);

    // file grid
    GridView grid = (GridView) findViewById(R.id.grid);
    grid.setEmptyView(findViewById(R.id.empty));
    grid.setAdapter(adapter);
    grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        File file = (File) adapter.getItem(position);
        try {
          if (ExternalStorageUtils.isImageFile(file)) {
            Message msg = Message.obtain();
            msg.what = Constant.Msg.Msg_Photo;
            msg.obj  = file;
            MainApp.broadcastMessage(msg);
          }
          else if (ExternalStorageUtils.isVideoFile(file) || ExternalStorageUtils.isAudioFile(file)) {
            Message msg = Message.obtain();
            msg.what = Constant.Msg.Msg_Play;
            msg.obj  = file;
            MainApp.broadcastMessage(msg);
          }
          else {
            toast("Error: Unknown file type");
          }
        }
        catch (Exception e) {
          toast("Error: " + e.getMessage());
        }
      }
    });

    startNetworkingService();
  }

  private void updateFolder(final String newFolder) {
    if (newFolder == null) return;

    handler.post(new Runnable() {
      @Override
      public void run() {
        TextView folder = (TextView) findViewById(R.id.folder);
        folder.setText(newFolder);
      }
    });
  }

  private void subtitle(final String message) {
    if (message == null) return;

    handler.post(new Runnable() {
      @Override
      public void run() {
        getActionBar().setSubtitle(message);
      }
    });
  }

  private void toast(final String message) {
    if (message == null) return;

    handler.post(new Runnable() {
      @Override
      public void run() {
        ToastUtils.showToast(DroidPlayActivity.this, message);
      }
    });
  }

  // ---------------------------------------------------------------------------
  // global Message listener

  private class DroidPlayHandler extends Handler {
    private WeakReference<DroidPlayActivity> activityRef;

    public DroidPlayHandler(DroidPlayActivity activity) {
      activityRef = new WeakReference<DroidPlayActivity>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);

      DroidPlayActivity activity = activityRef.get();

      if (activity == null)
        return;
      if (activity.isFinishing())
        return;

      switch (msg.what) {
        case Constant.Msg.Msg_AirPlay_Connect : {
          String receiver_name = (String) msg.obj;
          subtitle(receiver_name);
          break;
        }
        case Constant.Msg.Msg_AirPlay_Disconnect : {
          subtitle("Not connected");
          break;
        }
        case Constant.Msg.Msg_Exit_Service : {
          finish();
          break;
        }
      }

    }
  }

}
