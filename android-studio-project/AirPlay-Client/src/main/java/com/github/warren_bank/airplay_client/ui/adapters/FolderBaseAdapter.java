package com.github.warren_bank.airplay_client.ui.adapters;

import com.github.warren_bank.airplay_client.utils.ExternalStorageUtils;

import android.content.Context;
import android.widget.BaseAdapter;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Class used as a custom adapter for contents of the selected folder.
 *
 */
public abstract class FolderBaseAdapter extends BaseAdapter {

  // the application context
  protected Context context;

  // the current base folder
  protected File folder;

  // representation of images
  protected File[] files;

  /**
   * Initialize the adapter.
   *
   * @param context The application context
   */
  public FolderBaseAdapter(Context context, File folder) {
    super();
    this.context = context;
    this.folder = folder;
    refresh();
  }

  /**
   * Set the current folder (and refresh)
   *
   * @param folder The new folder
   */
  public void setFolder(File folder) {
    this.folder = folder;
    refresh();
  }

  /**
   * Return the current folder.
   *
   * @return The current folder
   */
  public File getFolder() {
    return (folder);
  }

  @Override
  public int getCount() {
    return (files.length);
  }

  @Override
  public Object getItem(int position) {
    return (files[position]);
  }

  @Override
  public long getItemId(int position) {
    return (position);
  }

  //
  // Protected
  //

  protected void refresh() {
    files = folder.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String filename) {
        return (ExternalStorageUtils.isImageFile(filename) || ExternalStorageUtils.isVideoFile(filename) || ExternalStorageUtils.isAudioFile(filename));
      }
    });

    notifyDataSetChanged();
  }
}
