package com.github.warren_bank.airplay_client.ui.adapters;

import com.github.warren_bank.airplay_client.R;
import com.github.warren_bank.airplay_client.utils.ExternalStorageUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

/**
 * Class used as a custom adapter for a detailed list of folder contents.
 *
 */
public class FolderListAdapter extends FolderBaseAdapter {

  /**
   * Initialize the adapter.
   *
   * @param context The application context
   */
  public FolderListAdapter(Context context, File folder) {
    super(context, folder);
  }

  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {
    File file = files[position];

    if (convertView == null) {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = inflater.inflate(R.layout.folder_item, null);
    }

    ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
    TextView  name = (TextView)  convertView.findViewById(R.id.name);

    name.setText(file.getName());
    icon.setVisibility(View.VISIBLE);

    if (ExternalStorageUtils.isImageFile(file))
      icon.setImageResource(R.drawable.file_icon_image);
    else if (ExternalStorageUtils.isVideoFile(file))
      icon.setImageResource(R.drawable.file_icon_video);
    else if (ExternalStorageUtils.isAudioFile(file))
      icon.setImageResource(R.drawable.file_icon_audio);
    else
      icon.setVisibility(View.INVISIBLE);

    return convertView;
  }
}
