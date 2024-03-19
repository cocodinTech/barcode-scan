package com.cocodin.barcodescan.plugin;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BarcodeScan extends CordovaPlugin {
  private static final String TAG = "BarcodeScan";
  public static final String CAMERA = "camera";
  public static final String C4050 = "c4050";
  public static final String NQUIRE300 = "NQuire300";

  public static final String EDA50K = "EDA50K";
  public static final String ZEBRAMC33 = "ZebraMC33";
  public static final String UNITECHEA300 = "UnitechEA300";

  public static final String EA630 = "EA630";
  public static final String IT51 = "IT_51";
  public static JSONArray jaDevices = new JSONArray(Arrays.asList(CAMERA, C4050, NQUIRE300, EDA50K, ZEBRAMC33, UNITECHEA300, EA630, IT51));
  private BaseScan mDevice;
  private Map<String, BaseScan> mDevices = new HashMap<String, BaseScan>();

  public void initialize(final CordovaInterface cordova, final CordovaWebView webView) {
    super.initialize(cordova, webView);
    cordova.getThreadPool().execute(new Runnable() {
      @Override
      public void run() {
        init(cordova, webView);
      }
    });
    Log.d(TAG, "Initializing BarcodeScan Plugin");
  }

  public void init(CordovaInterface cordova, CordovaWebView webView) {

    for (int i = 0; i < jaDevices.length(); i++) {
      try {
        String deviceName = jaDevices.getString(i);
        if (EDA50K.equalsIgnoreCase(deviceName)) {
          mDevices.put(EDA50K, new com.cocodin.barcodescan.plugin.devices.EDA50K(cordova, webView));
        } else if (C4050.equalsIgnoreCase(deviceName)) {
          mDevices.put(C4050, new com.cocodin.barcodescan.plugin.devices.C4050(cordova, webView));
        } else if (NQUIRE300.equalsIgnoreCase(deviceName)) {
          mDevices.put(NQUIRE300, new com.cocodin.barcodescan.plugin.devices.NQuire300(cordova, webView));
        } else if (ZEBRAMC33.equalsIgnoreCase(deviceName)) {
          mDevices.put(ZEBRAMC33, new com.cocodin.barcodescan.plugin.devices.ZebraMC33(cordova, webView));
        } else if (UNITECHEA300.equalsIgnoreCase(deviceName)) {
          mDevices.put(UNITECHEA300, new com.cocodin.barcodescan.plugin.devices.UnitechEA300(cordova, webView));
        } else if (EA630.equalsIgnoreCase(deviceName)) {
          mDevices.put(EA630, new com.cocodin.barcodescan.plugin.devices.EA630(cordova, webView));
        } else if (IT51.equalsIgnoreCase(deviceName)) {
          mDevices.put(IT51, new com.cocodin.barcodescan.plugin.devices.IT51(cordova, webView));
        } else {
          mDevices.put(CAMERA, new com.cocodin.barcodescan.plugin.devices.Camera(cordova, webView));
        }
      } catch (Exception e) {
        Log.e(TAG, e.getMessage());
      }
    }
  }

  public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext) {
    Log.d(TAG, "execute: " + action);
    if (action.equalsIgnoreCase("scan")) {
      cordova.getThreadPool().execute(new Runnable() {
        @Override
        public void run() {
          try {
            Object obj = args.get(0);
            String deviceName = "";
            if (obj instanceof String) {
              deviceName = ((String) obj);
            } else if (obj instanceof JSONObject) {
              deviceName = ((JSONObject) obj).get("device").toString();
            }
            //ensure device is created and is the correct device (user could change the device in mobile UI)
            if (mDevice == null || !mDevice.getDeviceName().equalsIgnoreCase(deviceName)) {
              mDevice = selectDevice(deviceName);
            }
            mDevice.scan(cordova, webView, args, callbackContext);
          } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            callbackContext.sendPluginResult(new PluginResult(Status.ERROR, e.getMessage()));
          }
        }
      });
    } else if (action.equalsIgnoreCase("enable")) {
      try {
        String deviceName = args.get(0).toString();
        //ensure device is created and is the correct device (user could change the device in mobile UI)
        if (mDevice == null || !mDevice.getDeviceName().equalsIgnoreCase(deviceName)) {
          mDevice = selectDevice(deviceName);
        }
        mDevice.enable(cordova, webView, args, callbackContext);
      } catch (Exception e) {
        Log.e(TAG, e.getMessage());
        callbackContext.sendPluginResult(new PluginResult(Status.ERROR, e.getMessage()));
      }
    } else if (action.equalsIgnoreCase("getDevices")) {
      callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, jaDevices));
    } else if (action.equalsIgnoreCase("launchAndroidSettings")) {
      BarcodeScan plugin = this;
      cordova.getThreadPool().execute(new Runnable() {
        @Override
        public void run() {
          try {
            String key = args.get(0).toString();
            Intent intent = new Intent();
            String pckg = cordova.getContext().getPackageName();
            if (key.equalsIgnoreCase("notifications") && Build.VERSION.SDK_INT >= 26) {
              intent.setAction(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
              intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, pckg);
            } else {
              intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
              intent.setData(Uri.parse("package:" + pckg));
            }
            cordova.startActivityForResult(plugin, intent, 0);
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "Launch OK"));
          } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            callbackContext.sendPluginResult(new PluginResult(Status.ERROR, e.getMessage()));
          }
        }
      });
    } else if (action.equalsIgnoreCase("play")) {
      cordova.getThreadPool().execute(new Runnable() {
        @Override
        public void run() {
          try {
            String fileName = args.get(0).toString();
            float vol = Float.parseFloat(args.get(1).toString());
            new AudioPlayer(cordova.getContext()).play(fileName, vol);
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "Launch OK"));
          } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            callbackContext.sendPluginResult(new PluginResult(Status.ERROR, e.getMessage()));
          }
        }
      });
    }
    return true;
  }

  public BaseScan selectDevice(String deviceName) {
    return mDevices.get(deviceName);
  }

  @Override
  public void onStart() {
    if (mDevice != null) {
      mDevice.onStart();
    }
    super.onStart();
  }

  @Override
  public void onStop() {
    if (mDevice != null) {
      mDevice.onStop();
    }
    super.onStop();
  }

  @Override
  public void onPause(boolean multiTasking) {
    if (mDevice != null) {
      mDevice.onPause(multiTasking);
    }
    super.onPause(multiTasking);
  }

  @Override
  public void onResume(boolean multiTasking) {
    if (mDevice != null) {
      mDevice.onResume(multiTasking);
    }
    super.onResume(multiTasking);
  }

  @Override
  public void onDestroy() {
    if (mDevice != null) {
      mDevice.onDestroy();
    }
    super.onDestroy();
  }

}
