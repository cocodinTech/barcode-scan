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

/**
 * Created by Diego Santiago on 7/2/22
 */

public class IT51 extends BaseScan {

    private static final String TAG = "IT_51";
    private final static String ACTION_TRIGGER_SOFTWARE_SCAN_KEY = "com.barcode.sendBroadcastScan";
    private final static String ACTION_RECEIVE_DATA = "com.barcode.sendBroadcast";


    private boolean IT51On = false;

    private BroadcastReceiver barcodeScannerBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) return;
            String barcodeStr = bundle.getString("BARCODE");
            if (null == barcodeStr || barcodeStr.isEmpty()) {
                return;
            }
            try {
                JSONObject obj = new JSONObject();
                obj.put("text", barcodeStr);
                PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                result.setKeepCallback(true);
                currentCallbackContext.sendPluginResult(result);
            } catch (JSONException e) {
                Log.e("Error: ", e.getMessage());
                currentCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
            }
        }

    };

    @Override
    public String getDeviceName() {
        return TAG;
    }

    public IT51(CordovaInterface cordova, CordovaWebView webView) {
        super(cordova, webView);
        initialize(cordova, webView);
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        initIT51(cordova.getContext());
    }

    @Override
    public void scan(CordovaInterface cordova, CordovaWebView webView, JSONArray args,
                     CallbackContext callbackContext) {
        Context context = cordova.getActivity();
        if (!IT51On) {
            initIT51(context);
        }
/*        Bundle bundle = new Bundle();
        bundle.putBoolean("scan", true);*/
        Intent intent = new Intent().setAction(ACTION_TRIGGER_SOFTWARE_SCAN_KEY);//.putExtras(bundle);
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

    private void initIT51(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_RECEIVE_DATA);
        context.registerReceiver(barcodeScannerBroadcastReceiver, filter);
        IT51On = true;
    }
}