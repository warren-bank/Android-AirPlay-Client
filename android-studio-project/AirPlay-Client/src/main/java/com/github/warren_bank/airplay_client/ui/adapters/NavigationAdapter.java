package com.github.warren_bank.airplay_client.ui.adapters;

import com.github.warren_bank.airplay_client.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class NavigationAdapter extends ArrayAdapter<NavigationItem> {

  public NavigationAdapter(Context context, List<NavigationItem> items) {
    super(context, 0, items);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View view = inflater.inflate(R.layout.drawer_item, null);
    NavigationItem item = getItem(position);
    ImageView icon = (ImageView) view.findViewById(R.id.icon);
    icon.setImageResource(item.getIcon());
    TextView name = (TextView) view.findViewById(R.id.name);
    name.setText(item.getName());
    return view;
  }

}
