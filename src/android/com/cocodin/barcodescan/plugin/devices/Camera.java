package com.cocodin.barcodescan.plugin.devices;

import com.cocodin.barcodescan.plugin.BaseScan;
import com.phonegap.plugins.barcodescanner.BarcodeScanner;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by alberto.doval on 22/10/17.
 */

public class Camera extends BaseScan {

    private static final String TAG = "camera";

    @Override
    public String getDeviceName() {
        return TAG;
    }

    public Camera(CordovaInterface cordova, CordovaWebView webView) {
        super(cordova, webView);
        initialize(cordova, webView);
    }

    public void enable(CordovaInterface cordova, CordovaWebView webView, JSONArray args, final CallbackContext callbackContext) {
        super.enable(cordova, webView, args, callbackContext);
    };

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {

    }

    @Override
    public void scan(CordovaInterface cordova, CordovaWebView webView, JSONArray args, CallbackContext callbackContext) {
        BarcodeScanner barcodeScanner = new BarcodeScanner();
        barcodeScanner.cordova = cordova;
        barcodeScanner.webView = webView;
        barcodeScanner.execute("scan", args, callbackContext);
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