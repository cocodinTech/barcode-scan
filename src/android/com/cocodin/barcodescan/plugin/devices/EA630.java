package com.cocodin.barcodescan.plugin.devices;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.cocodin.barcodescan.plugin.BaseScan;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Diego Santiago on 4/2/22
 * <a href="https://www.ute.com/uploads/images/Downloads/Android_%20Programming_Manual%20V2_8_1.pdf">...</a>
 */

public class EA630 extends BaseScan {

  private Map<Integer, String> mapTypes = new HashMap<>();

  private static final String TAG = "UnitechEA630";
  private final static String ACTION_SCAN2KEY_SETTING = "unitech.scanservice.scan2key_setting";
  private final static String ACTION_TRIGGER_SOFTWARE_SCAN_KEY = "unitech.scanservice.software_scankey";
  private final static String ACTION_INIT_INTENT = "unitech.scanservice.init";
  private final static String ACTION_RECEIVE_DATA = "unitech.scanservice.data";
  private final static String ACTION_RECEIVE_DATABYTES = "unitech.scanservice.databyte";
  private final static String ACTION_RECEIVE_DATALENGTH = "unitech.scanservice.datalength";
  private final static String ACTION_RECEIVE_DATATYPE = "unitech.scanservice.datatype";
  private final static String ACTION_RECEIVE_TERMINATOR = "unitech.scanservice.terminator";
  private boolean EA630On = false;


  private final BroadcastReceiver barcodeScannerBroadcastReceiver = new BroadcastReceiver() {
    private JSONObject obj;

    @Override
    public void onReceive(Context context, Intent intent) {
      if (currentCallbackContext == null) return;
      Bundle bundle = intent.getExtras();
      if (bundle == null) return;
      String barcodeStr = bundle.getString("text");
      int type = bundle.getInt("text");
      if (barcodeStr != null && barcodeStr.indexOf("\n") == barcodeStr.length() - 1) {
        barcodeStr = barcodeStr.replaceAll("[\n]", "");
      }
      try {
        if (null == obj && barcodeStr != null) {
          obj = new JSONObject();
          obj.put("text", barcodeStr);
        } else if (type != 0 && obj != null) {
          obj.put("format", mapTypes.get(type));
          PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
          result.setKeepCallback(true);
          currentCallbackContext.sendPluginResult(result);
          obj = null;
        }
      } catch (JSONException e) {
        Log.e("Error: ", Objects.requireNonNull(e.getMessage()));
        currentCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
      }
    }

  };

  @Override
  public String getDeviceName() {
    return TAG;
  }

  public EA630(CordovaInterface cordova, CordovaWebView webView) {
    super(cordova, webView);
    initialize(cordova, webView);
  }

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    initEA630(cordova.getContext());
  }

  @Override
  public void scan(CordovaInterface cordova, CordovaWebView webView, JSONArray args,
                   CallbackContext callbackContext) {
    Context context = cordova.getActivity();
    if (!EA630On) {
      initEA630(context);
    }
    Bundle bundle = new Bundle();
    bundle.putBoolean("scan", true);
    Intent intent = new Intent().setAction(ACTION_TRIGGER_SOFTWARE_SCAN_KEY).putExtras(bundle);
    context.sendBroadcast(intent);

  }

  @Override
  public void onStart() {

  }

  @Override
  public void onStop() {

  }

  @Override
  public void onDestroy() {

  }

  @Override
  public void onResume(boolean multitasking) {

  }

  @Override
  public void onPause(boolean multitasking) {

  }

  private void initEA630(Context context) {
    this.mapTypes.put(100, "EAN_13");
    this.mapTypes.put(68, "EAN_8");
    this.mapTypes.put(98, "CODE_39");
    this.mapTypes.put(106, "CODE_128");
    //this.mapTypes.put(73, "EAN_14");
    this.mapTypes.put(99, "UPC_A");
    this.mapTypes.put(69, "UPC_E");
    this.mapTypes.put(115, "QR_CODE");
    this.mapTypes.put(119, "DATA_MATRIX");

    IntentFilter filter = new IntentFilter();
    filter.addAction(ACTION_RECEIVE_DATA);
    filter.addAction(ACTION_RECEIVE_DATATYPE);

     /*
     filter.addAction(ACTION_RECEIVE_TERMINATOR);
     filter.addAction(ACTION_RECEIVE_DATABYTES);
     filter.addAction(ACTION_RECEIVE_DATALENGTH);
     */
    context.registerReceiver(barcodeScannerBroadcastReceiver, filter); //todo Context.RECEIVER_NOT_EXPORTED
    // deshabilitar scan2key, para recibir datos en onReceive()
    Bundle bundle = new Bundle();
    bundle.putBoolean("scan2key", false);

    Intent intent = new Intent().setAction(ACTION_SCAN2KEY_SETTING).putExtras(bundle);
    context.sendBroadcast(intent);
    EA630On = true;

  }
}
