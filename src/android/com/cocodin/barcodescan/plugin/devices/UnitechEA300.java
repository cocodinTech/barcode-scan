package com.cocodin.barcodescan.plugin.devices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.device.ScanManager;
import android.device.scanner.configuration.PropertyID;
import android.device.scanner.configuration.Triggering;
import android.os.Vibrator;

import com.cocodin.barcodescan.plugin.BaseScan;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

public class UnitechEA300 extends BaseScan {

    private static final String TAG = "UnitechEA300";

    private final static String SCAN_ACTION = ScanManager.ACTION_DECODE;//default action

    private Vibrator mVibrator;
    private ScanManager mScanManager;

    private String barcodeStr;
    private CordovaWebView webView;

    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            //Received read
            if (mVibrator != null) {
                mVibrator.vibrate(50);
            }

            byte[] barcode = intent.getByteArrayExtra(ScanManager.DECODE_DATA_TAG);
            int barcodelen = intent.getIntExtra(ScanManager.BARCODE_LENGTH_TAG, 0);
            byte temp = intent.getByteExtra(ScanManager.BARCODE_TYPE_TAG, (byte) 0);
            android.util.Log.d(TAG, " Code type received: " + temp);
            barcodeStr = new String(barcode, 0, barcodelen);

            if (currentCallbackContext != null) {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("text", barcodeStr);
                    PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                    result.setKeepCallback(true);
                    currentCallbackContext.sendPluginResult(result);
                } catch (Exception x) {
                    BaseScan.sendPluginResultError(currentCallbackContext, x.getMessage());
                }
            }
        }

    };

    public UnitechEA300(CordovaInterface cordova, CordovaWebView webView) {
        super(cordova, webView);
        initialize(cordova, webView);
    }

    public void enable(CordovaInterface cordova, CordovaWebView webView, JSONArray args,
                       final CallbackContext callbackContext) {
        this.currentCallbackContext = callbackContext;
        JSONObject obj = new JSONObject();
        if(mScanManager == null) {
            initScanner();
        }
        PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    };

    @Override
    public void scan(CordovaInterface cordova, CordovaWebView webView, JSONArray args, final CallbackContext callbackContext) {
        this.currentCallbackContext = callbackContext;

        if(mScanManager == null) {
            initScanner();
        }
        mScanManager.startDecode();
    }

    @Override
    public String getDeviceName() {
        return TAG;
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        this.webView = webView;
        mVibrator = (Vibrator) cordova.getActivity().getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void initScanner() {
        mScanManager = new ScanManager();
        mScanManager.openScanner();
        mScanManager.switchOutputMode( 0);
        if(mScanManager.getTriggerMode() != Triggering.HOST) {
            mScanManager.setTriggerMode(Triggering.HOST);
        }
       registerReceiver();
    }

    @Override
    public void onStart() {

    };

    @Override
    public void onStop() {

    };

    @Override
    public void onDestroy() {

    };

    @Override
    public void onResume(boolean multitasking) {
        initScanner();
        registerReceiver();
    };



    @Override
    public void onPause(boolean multitasking) {
        if(mScanManager != null) {
            mScanManager.stopDecode();
        }
        unregisterReceiver();
    };


    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        int[] idbuf = new int[]{PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG};
        String[] value_buf = mScanManager.getParameterString(idbuf);
        if(value_buf != null && value_buf[0] != null && !value_buf[0].equals("")) {
            filter.addAction(value_buf[0]);
        } else {
            filter.addAction(SCAN_ACTION);
        }

        this.webView.getContext().registerReceiver(mScanReceiver, filter);
    }


    private void unregisterReceiver() {
        this.webView.getContext().unregisterReceiver(mScanReceiver);
    }

}
