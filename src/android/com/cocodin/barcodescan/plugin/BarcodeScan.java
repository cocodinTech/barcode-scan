/**
 */
package com.cocodin.barcodescan.plugin;

import android.util.Log;

import com.cocodin.barcodescan.plugin.devices.*;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static android.R.attr.action;

public class BarcodeScan extends CordovaPlugin {

  private static final String TAG = "BarcodeScan";

  public static final String CAMERA = "camera";

  public static final String C4050 = "c4050";

  public static final String NQUIRE300 = "NQuire300";

  public static final String EDA50K = "EDA50K";

  public static JSONArray jaDevices = new JSONArray(Arrays.asList(CAMERA, C4050, NQUIRE300, EDA50K));

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

    for (int i=0;i<jaDevices.length();i++) {
      try {
        String deviceName = jaDevices.getString(i);
        if (EDA50K.equalsIgnoreCase(deviceName)) {
          mDevices.put(EDA50K, new com.cocodin.barcodescan.plugin.devices.EDA50K(cordova, webView));
        } else if (C4050.equalsIgnoreCase(deviceName)) {
          mDevices.put(C4050, new com.cocodin.barcodescan.plugin.devices.C4050(cordova, webView));
        } else if (NQUIRE300.equalsIgnoreCase(deviceName)) {
          mDevices.put(NQUIRE300, new com.cocodin.barcodescan.plugin.devices.NQuire300(cordova, webView));
        } else {
          mDevices.put(CAMERA, new com.cocodin.barcodescan.plugin.devices.Camera(cordova, webView));
        }
      }
      catch (Exception e) {
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
            String deviceName = args.get(0).toString();
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
    }
    else if (action.equalsIgnoreCase("getDevices")) {
      callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, jaDevices));
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
