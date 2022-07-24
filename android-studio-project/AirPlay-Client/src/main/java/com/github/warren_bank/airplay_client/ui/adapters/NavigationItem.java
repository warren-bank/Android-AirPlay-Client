package com.github.warren_bank.airplay_client.ui.adapters;

public class NavigationItem {
  private String tag;
  private String name;
  private int icon;

  public NavigationItem(String tag, String name, int icon) {
    this.tag  = tag;
    this.name = name;
    this.icon = icon;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getIcon() {
    return icon;
  }

  public void setIcon(int icon) {
    this.icon = icon;
  }
}
