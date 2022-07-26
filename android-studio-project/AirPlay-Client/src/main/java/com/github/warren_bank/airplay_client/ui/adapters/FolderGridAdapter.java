package com.github.warren_bank.airplay_client.ui.adapters;

import com.github.warren_bank.airplay_client.R;
import com.github.warren_bank.airplay_client.utils.ExternalStorageUtils;
import com.github.warren_bank.airplay_client.utils.ImageUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Class used as a custom adapter for thumbnail images in main grid. Supports scaling down and lazy loading images.
 *
 * @author Tuomas Tikka
 */
public class FolderGridAdapter extends FolderBaseAdapter {

  /**
   * Initialize the adapter.
   *
   * @param context The application context
   */
  public FolderGridAdapter(Context context, File folder) {
    super(context, folder);
  }

  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {
    ImageView imageView = null;
    if (convertView == null) {
      imageView = new ImageView(context);
      imageView.setLayoutParams(new GridView.LayoutParams(128, 128));
      imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
      imageView.setPadding(8, 8, 8, 8);
      imageView.setImageResource(R.drawable.file_placeholder);
    } else {
      imageView = (ImageView) convertView;
    }
    File file = files[position];
    new ImageAsyncTask(imageView).execute(file);
    return (imageView);
  }

  //
  // Private
  //

  private class ImageAsyncTask extends AsyncTask<File, Void, Bitmap> {
    private WeakReference<ImageView> imageViewReference;

    public ImageAsyncTask(ImageView imageView) {
      this.imageViewReference = new WeakReference<ImageView>(imageView);
    }

    @Override
    protected Bitmap doInBackground(File... params) {
      if (imageViewReference == null) return null;

      File file = params[0];

      if (ExternalStorageUtils.isImageFile(file)) {
        Bitmap bitmap = ImageUtils.createImageThumbnail(file, 128, 128);
        return bitmap;
      }

      if (ExternalStorageUtils.isVideoFile(file)) {
        Bitmap bitmap = ImageUtils.createVideoThumbnail(context, file, 128, 128);
        return bitmap;
      }

      if (ExternalStorageUtils.isAudioFile(file)) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.file_audio);
        return bitmap;
      }

      return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
      if ((imageViewReference != null) && (bitmap != null)) {
        ImageView imageView = imageViewReference.get();
        if (imageView != null) {
          imageView.setImageBitmap(bitmap);
        }
      }
    }
  }
}
