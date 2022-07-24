package com.github.warren_bank.airplay_client.utils;

import com.github.warren_bank.airplay_client.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ToastUtils {

  public static void showToast(Context context, String message) {
    ToastUtils.showToast(context, message, Toast.LENGTH_SHORT);
  }

  public static void showToast(Context context, String message, int duration) {
    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View layout = inflater.inflate(R.layout.toast, null);
    TextView text = (TextView) layout.findViewById(R.id.text);
    text.setText(message);
    Toast toast = new Toast(context.getApplicationContext());
    toast.setDuration(duration);
    toast.setView(layout);
    toast.show();
  }

}
