package com.cocodin.barcodescan.plugin.devices;

import android.util.Log;
import android.widget.Toast;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cocodin.barcodescan.plugin.BaseScan;
import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.AidcManager.CreatedCallback;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.BarcodeReader.BarcodeListener;
import com.honeywell.aidc.ScannerUnavailableException;
import com.honeywell.aidc.UnsupportedPropertyException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alberto.doval on 22/10/17.
 */

public class EDA50K extends BaseScan implements BarcodeListener {

    private static String TAG = EDA50K.class.getName();

    private BarcodeReader barcodeReader;
    private AidcManager manager;

    //private CallbackContext currentCallbackContext = null;

    public EDA50K(CordovaInterface cordova, CordovaWebView webView) {
        super(cordova, webView);
        initialize(cordova, webView);
    }

    @Override
    public String getDeviceName() {
        return TAG;
    }

    public void enable(CordovaInterface cordova, CordovaWebView webView, JSONArray args,
            final CallbackContext callbackContext) {
        this.currentCallbackContext = callbackContext;
        JSONObject obj = new JSONObject();
        PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    };

    public void initialize(final CordovaInterface cordova, CordovaWebView webView) {
        if (manager == null) {
            AidcManager.create(cordova.getActivity(), new CreatedCallback() {

                @Override
                public void onCreated(AidcManager aidcManager) {
                    manager = aidcManager;
                    barcodeReader = manager.createBarcodeReader();

                    if (barcodeReader != null) {

                        barcodeReader.addBarcodeListener(EDA50K.this);

                        // Map<String, Object> asdfsadf =
                        // barcodeReader.getAllDefaultProperties();
                        // for (String key : asdfsadf.keySet()) {
                        // Log.d("chromium", key + "=" + asdfsadf.get(key));
                        // }

                        // set the trigger mode to auto control
                        try {
                            barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                                    BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
                        } catch (UnsupportedPropertyException e) {
                            Toast.makeText(cordova.getActivity(), "Failed to apply properties", Toast.LENGTH_SHORT)
                                    .show();
                        }

                        Map<String, Object> properties = new HashMap<String, Object>();
                        // Set Symbologies On/Off
                        properties.put(BarcodeReader.PROPERTY_CODE_128_ENABLED, true);
                        properties.put(BarcodeReader.PROPERTY_GS1_128_ENABLED, true);
                        properties.put(BarcodeReader.PROPERTY_QR_CODE_ENABLED, true);
                        properties.put(BarcodeReader.PROPERTY_CODE_39_ENABLED, true);
                        properties.put(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true);
                        properties.put(BarcodeReader.PROPERTY_UPC_A_ENABLE, true);
                        properties.put(BarcodeReader.PROPERTY_EAN_13_ENABLED, true);
                        properties.put(BarcodeReader.PROPERTY_AZTEC_ENABLED, false);
                        properties.put(BarcodeReader.PROPERTY_CODABAR_ENABLED, false);
                        properties.put(BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED, false);
                        properties.put(BarcodeReader.PROPERTY_PDF_417_ENABLED, false);
                        // Set Max Code 39 barcode length
                        properties.put(BarcodeReader.PROPERTY_CODE_39_MAXIMUM_LENGTH, 48);
                        // Turn on center decoding
                        properties.put(BarcodeReader.PROPERTY_CENTER_DECODE, true);
                        // Disable bad read response, handle in onFailureEvent
                        properties.put(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, false);

                        properties.put(BarcodeReader.PROPERTY_DATA_PROCESSOR_LAUNCH_BROWSER, false);

                        // Apply the settings
                        barcodeReader.setProperties(properties);

                        //Perform first read after initialization
                        claim();

                    }
                    ;
                }
            });
        }
    }

    @Override
    public void scan(final CordovaInterface cordova, CordovaWebView webView, JSONArray args,
            final CallbackContext callbackContext) {
        this.currentCallbackContext = callbackContext;
        try {
            //force trigger
            barcodeReader.softwareTrigger(true);
            JSONObject obj = new JSONObject();
            PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
            result.setKeepCallback(true);
            currentCallbackContext.sendPluginResult(result);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
            result.setKeepCallback(true);
            currentCallbackContext.sendPluginResult(result);
        }
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onResume(boolean multitasking) {
        if (barcodeReader != null) {
            try {
                barcodeReader.claim();
            } catch (ScannerUnavailableException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void onPause(boolean multitasking) {
        if (barcodeReader != null) {
            // release the scanner claim so we don't get any scanner
            // notifications while paused.
            barcodeReader.release();
        }
    }

    private void claim() {
        if (barcodeReader != null) {
            try {
                barcodeReader.claim();
                Log.d(TAG, "Claim OK");
            } catch (ScannerUnavailableException e) {
                Log.e(TAG, e.getMessage());
            }
        } else {
            Log.e(TAG, "Barcode reader not opened");
        }
    }

    @Override
    public void onDestroy() {
        if (barcodeReader != null) {
            // close BarcodeReader to clean up resources.
            barcodeReader.close();
            barcodeReader = null;
        }

        if (manager != null) {
            // close AidcManager to disconnect from the scanner service.
            // once closed, the object can no longer be used.
            manager.close();
        }
    }

    @Override
    public void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent) {
        Log.d(TAG + " - Barcode: ", barcodeReadEvent.getBarcodeData());

        if (currentCallbackContext != null) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("text", barcodeReadEvent.getBarcodeData());
                PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                barcodeReader.softwareTrigger(false);
                result.setKeepCallback(true);
                currentCallbackContext.sendPluginResult(result);
            } catch (Exception x) {
                PluginResult result = new PluginResult(PluginResult.Status.ERROR, x.getMessage());
                result.setKeepCallback(true);
                currentCallbackContext.sendPluginResult(result);
            }
        }
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {
        Log.i(TAG, "Error reading barcode.");
        if (currentCallbackContext != null) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("success", false);

                PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                result.setKeepCallback(true);
                currentCallbackContext.sendPluginResult(result);
            } catch (Exception x) {
                Log.e(TAG, x.getMessage());
            }
        }
    }
}
