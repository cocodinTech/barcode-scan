package com.cocodin.barcodescan.plugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by alberto.doval on 21/10/17.
 */

public abstract class BaseScan {

    protected CallbackContext currentCallbackContext;

    public BaseScan(CordovaInterface cordova, CordovaWebView webView) {

    };

    public abstract void initialize(CordovaInterface cordova, CordovaWebView webView);

     public void enable(CordovaInterface cordova, CordovaWebView webView, JSONArray args, final CallbackContext callbackContext) {
        this.currentCallbackContext = callbackContext;
        JSONObject obj = new JSONObject();
        PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    };

    public abstract String getDeviceName();

    public abstract void scan(final CordovaInterface cordova, CordovaWebView webView, JSONArray args, final CallbackContext callbackContext);

    public abstract void onStart();

    public abstract void onStop();

    public abstract void onDestroy();

    public abstract void onResume(boolean multitasking);

    public abstract void onPause(boolean multitasking);

}
