package com.zmxv.RNSound;

import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.content.res.AssetFileDescriptor;
import android.util.Log;
import android.media.AudioManager;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;

import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.android.vending.expansion.zipfile.ZipResourceFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.util.Arrays;

public class RNSoundModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
  SoundPool playerPool = new SoundPool(9, AudioManager.STREAM_MUSIC, 0);
  Map<String, Integer> keys = new HashMap<>();
  ReactApplicationContext context;
  final static Object NULL = null;
  private static final String TAG = "RNSoundModule";

  public RNSoundModule(ReactApplicationContext context) {
    super(context);
    context.addLifecycleEventListener(this);
    this.context = context;
  }

  @Override
  public String getName() {
    return "RNSound";
  }

  @ReactMethod
  public void checkExpansionFile(
    final Integer ver, 
    final Integer patch,
    final String uri,
    final Callback errCallabck) 
  {
    ZipResourceFile expansionFile = null;
    AssetFileDescriptor fd = null;
    try {
        expansionFile = APKExpansionSupport.getAPKExpansionZipFile(this.context, ver, patch);
        fd = expansionFile.getAssetFileDescriptor(uri);
    } catch (IOException e) {
        errCallabck.invoke(e.getMessage());
        return;
    } catch (NullPointerException e) {
        errCallabck.invoke(e.getMessage());
        return;
    }
    if(fd==null) {
      errCallabck.invoke("No file");
    } else {
      try {
        fd.close();
      } catch (IOException e) {
        Log.e(TAG, Arrays.toString(e.getStackTrace()));
        e.getStackTrace();
      }
    } 
  }


  @ReactMethod
  public void prepare(final String fileName, final String key, final Callback callback) {

    Boolean player = createMediaPlayer(fileName, key);
    if (player == false) {
      WritableMap e = Arguments.createMap();
      e.putInt("code", -1);
      e.putString("message", "resource not found");
      callback.invoke(e);
      return;
    }
    callback.invoke(NULL);
  }

  protected Boolean createMediaPlayer(final String fileName,final String key) {
    if(fileName.startsWith("exp://")) {
      String[] path = fileName.split("//");
      Integer expVer = Integer.parseInt(path[1]);
      Integer expPatchVer = Integer.parseInt(path[2]);
      String uri = path[3];
      ZipResourceFile expansionFile = null;
      AssetFileDescriptor fd = null;
      if(expVer>0) {
          try {
              expansionFile = APKExpansionSupport.getAPKExpansionZipFile(this.context, expVer, expPatchVer);
              fd = expansionFile.getAssetFileDescriptor(uri);
          } catch (IOException e) {
              Log.e(TAG, Arrays.toString(e.getStackTrace()));
              e.getStackTrace();
          } catch (NullPointerException e) {
              Log.e(TAG, Arrays.toString(e.getStackTrace()));
              e.getStackTrace();
          }
      }
      if(fd!=null) {
        try {
          Integer id = this.playerPool.load(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength(), 1);
          this.keys.put(key, id);
          Log.i(TAG, String.valueOf(id) + key);
        } catch (NullPointerException e) {
            Log.e(TAG, Arrays.toString(e.getStackTrace()));
            e.getStackTrace();
        }
        try {
          fd.close();
        } catch (IOException e) {
          Log.e(TAG, Arrays.toString(e.getStackTrace()));
          e.getStackTrace();
        }
        return true;
      }
    } 
    return false;
  }

  @ReactMethod
  public void play(final String key, final Float volume) {
    Integer id = this.keys.get(key);
    Log.i(TAG, "play "+String.valueOf(id) + key + " vol: "+String.valueOf(volume));
    id = this.playerPool.play(id, volume, volume, 1, -1, 1);
    this.keys.put("stream_"+key, id);
  }

  @ReactMethod
  public void pause(final String key) {
    Integer id = this.keys.get("stream_"+key);
    this.playerPool.pause(id);
  }

  @ReactMethod
  public void stop(final String key) {
    Integer id = this.keys.get("stream_"+key);
    Log.i(TAG, "stop "+String.valueOf(id) + key);
    this.playerPool.stop(id);
  }

  @ReactMethod
  public void setVolume(final String key, final Float left, final Float right) {
    if(this.keys.get("stream_"+key)==null)
      return;
    Integer id = this.keys.get("stream_"+key);
    Log.i(TAG, "vol "+String.valueOf(id) +" - " +  String.valueOf(left));
    this.playerPool.setVolume(id, left, right);
  }

  @ReactMethod
  public void setLooping(final String key, final Integer looping) {
    Integer id = this.keys.get("stream_"+key);
    this.playerPool.setLoop(id, looping);
  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    constants.put("IsAndroid", true);
    return constants;
  }

  public void onDestroy() {
    this.playerPool.release();
  }

  public void onHostDestroy() {
    this.playerPool.release();
  }

  public void onHostPause() {}

  public void onHostResume() {}
}

