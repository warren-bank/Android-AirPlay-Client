package com.github.warren_bank.airplay_client.utils;

import com.github.warren_bank.airplay_client.R;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.File;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ExternalStorageUtils {

  // --------------------------------------------------------------------------- media type detection

  // ===================================
  // filename extension

  private static String get_fileExtension(String filename, Pattern mediatype_regex) {
    return get_fileExtension(filename, mediatype_regex, /* capture_group_index= */ 1);
  }

  private static String get_fileExtension(String filename, Pattern mediatype_regex, int capture_group_index) {
    if (filename == null) return null;

    Matcher matcher = mediatype_regex.matcher(filename.toLowerCase());
    String file_ext = matcher.find()
      ? matcher.group(capture_group_index)
      : null;

    return file_ext;
  }

  // ===================================
  // video

  private static Pattern video_regex = Pattern.compile("\\.(mp4|mp4v|mpv|m1v|m4v|mpg|mpg2|mpeg|xvid|webm|3gp|avi|mov|mkv|ogg|ogv|ogm|m3u8|mpd|ism[vc]?)$");

  public static String get_video_fileExtension(String filename) {
    return get_fileExtension(filename, video_regex);
  }

  public static boolean isVideoFile(String filename) {
    String file_ext = get_video_fileExtension(filename);
    return (file_ext != null);
  }

  public static boolean isVideoFile(File file) {
    return ((file != null) && isVideoFile(file.getName()));
  }

  // ===================================
  // audio

  private static Pattern audio_regex = Pattern.compile("\\.(mp3|m4a|ogg|wav|flac|m3u)$");

  public static String get_audio_fileExtension(String filename) {
    return get_fileExtension(filename, audio_regex);
  }

  public static boolean isAudioFile(String filename) {
    String file_ext = get_audio_fileExtension(filename);
    return (file_ext != null);
  }

  public static boolean isAudioFile(File file) {
    return ((file != null) && isAudioFile(file.getName()));
  }

  // ===================================
  // image

  // https://developer.android.com/guide/topics/media/media-formats#image-formats
  private static Pattern image_regex = Pattern.compile("\\.(bmp|gif|jp[e]?g|png|webp|hei[cf])$");

  public static String get_image_fileExtension(String filename) {
    return get_fileExtension(filename, image_regex);
  }

  public static boolean isImageFile(String filename) {
    String file_ext = get_image_fileExtension(filename);
    return (file_ext != null);
  }

  public static boolean isImageFile(File file) {
    return ((file != null) && isImageFile(file.getName()));
  }

}
