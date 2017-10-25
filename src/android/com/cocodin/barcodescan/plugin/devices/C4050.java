package com.cocodin.barcodescan.plugin.devices;

import android.content.Context;
import android.util.Log;

import com.cocodin.barcodescan.plugin.BaseScan;
import com.zebra.adc.decoder.Barcode2DWithSoft;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.cordova.PluginResult.Status;


/**
 * Created by alberto.doval on 22/10/17.
 */

public class C4050 extends BaseScan {

    private static final String TAG = "C4050";

    private static Barcode2DWithSoft mInstance;

    public C4050(CordovaInterface cordova, CordovaWebView webView) {
        super(cordova, webView);
        initialize(cordova, webView);
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {

    }

    @Override
    public String getDeviceName() {
        return TAG;
    }

    @Override
    public void scan(CordovaInterface cordova, CordovaWebView webView, JSONArray args, final CallbackContext callbackContext) {
        Context context = cordova.getActivity();
        try {
            mInstance = Barcode2DWithSoft.getInstance();
            if (mInstance != null) {
                mInstance.open(context);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            callbackContext.sendPluginResult(new PluginResult(Status.ERROR, e.getMessage()));
        }
        mInstance.setScanCallback(new Barcode2DWithSoft.ScanCallback() {
            @Override
            public void onScanComplete(int i, int length, byte[] data) {
                Log.i(TAG, "onScanComplete() i=" + i);

                if (length < 1) {
                    return;
                }

                String barCode = new String(data);
                barCode = barCode.replaceAll("\u0000", "");
                Log.i(TAG, barCode);

                try {
                    JSONObject result = new JSONObject().put("text", barCode);
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, result));
                } catch (JSONException e) {
                    callbackContext.sendPluginResult(new PluginResult(Status.ERROR, e.getMessage()));
                }
            }
        });
        mInstance.scan();
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
}

