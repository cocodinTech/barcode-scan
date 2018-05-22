package com.cocodin.barcodescan.plugin.devices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.cocodin.barcodescan.plugin.BaseScan;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by alberto.doval on 22/10/17.
 */

public class NQuire300 extends BaseScan {

    private static final String TAG = "NQuire300";

    private boolean MQuireOn = false;

    private BroadcastReceiver barcodeScannerBroadcastReceiver;

    @Override
    public String getDeviceName() {
        return TAG;
    }

    public NQuire300(CordovaInterface cordova, CordovaWebView webView) {
        super(cordova, webView);
        initialize(cordova, webView);
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {

    }

    @Override
    public void scan(CordovaInterface cordova, CordovaWebView webView, JSONArray args, CallbackContext callbackContext) {
        Context context = cordova.getActivity();
        //cbContext = callbackContext;
        if (!MQuireOn) {
            initMquire(context, callbackContext);
        }
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

    private void initMquire(Context context, final CallbackContext callbackContext) {
        Intent intent = new Intent("ACTION_BAR_SCANCFG");
        intent.putExtra("EXTRA_SCAN_MODE", 3);
        intent.putExtra("EXTRA_SCAN_AUTOENT", 0);
        intent.putExtra("EXTRA_SCAN_NOTY_LED", 1);
        intent.putExtra("EXTRA_SCAN_NOTY_SND", 1);
        context.sendBroadcast(intent);
        MQuireOn = true;
        barcodeScannerBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String scanResult_1 = intent.getStringExtra("SCAN_BARCODE1");
                final String scanStatus = intent.getStringExtra("SCAN_STATE");
                if (null == scanResult_1 || null == scanStatus || scanResult_1.isEmpty() || scanStatus.isEmpty()) {
                    return;
                }

                if ("ok".equals(scanStatus)) {
                    callbackContext.success(scanResult_1);
                }
            }
        };
        context.registerReceiver(barcodeScannerBroadcastReceiver, new IntentFilter("nlscan.action.SCANNER_RESULT"));
    }
}