package com.cocodin.barcodescan.plugin.devices;

import android.util.Log;

import com.cocodin.barcodescan.plugin.BaseScan;
import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.EMDKManager.FEATURE_TYPE;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.BarcodeManager.ConnectionState;
import com.symbol.emdk.barcode.BarcodeManager.ScannerConnectionListener;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.ScanDataCollection.ScanData;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.Scanner.DataListener;
import com.symbol.emdk.barcode.Scanner.StatusListener;
import com.symbol.emdk.barcode.Scanner.TriggerType;
import com.symbol.emdk.barcode.ScannerConfig;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.ScannerInfo;
import com.symbol.emdk.barcode.ScannerResults;
import com.symbol.emdk.barcode.StatusData;
import com.symbol.emdk.barcode.StatusData.ScannerStates;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by alberto.doval on 21/05/18.
 */

public class ZebraMC33 extends BaseScan implements EMDKListener, DataListener, StatusListener, ScannerConnectionListener {

    private static final String TAG = "ZebraMC33";

    private EMDKManager emdkManager = null;
    private BarcodeManager barcodeManager = null;
    private Scanner scanner = null;

    private boolean bContinuousMode = true;

    private List<ScannerInfo> deviceList = null;

    private int scannerIndex = 1; // Keep the selected scanner
    private int defaultIndex = 0; // Keep the default scanner
    private int triggerIndex = 0;
    private int dataLength = 0;
    private String statusString = "";
    private Map<String, String> mapTypes = new HashMap<>();

    public ZebraMC33(CordovaInterface cordova, CordovaWebView webView) {
        super(cordova, webView);
        initialize(cordova, webView);
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        init(cordova);
    }

    @Override
    public String getDeviceName() {
        return TAG;
    }

    public void init(CordovaInterface cordova) {
        //when initialised, it call automatically onOpened
        EMDKResults results = EMDKManager.getEMDKManager(cordova.getActivity().getApplicationContext(), this);
        if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            Log.e(TAG, "Status: " + "EMDKManager object request failed!");
            return;
        }
    }

    /* //Only needed in continousMode=false
        public void prepareForScan() {
        try {
            if(scanner == null) {
                initScanner();
            }
            scanner.cancelRead();
            if (scanner.isEnabled()) {
                scanner.read();
            }
        } catch (ScannerException e) {
            Log.e(TAG,"Status: " + e.getMessage());
        }
    }*/

    public void enable(CordovaInterface cordova, CordovaWebView webView, JSONArray args, final CallbackContext callbackContext) {
        this.currentCallbackContext = callbackContext;
        //this.prepareForScan();
        JSONObject obj = new JSONObject();
        PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    ;

    private void initScanner() {
        this.mapTypes.put("EAN13", "EAN_13");
        this.mapTypes.put("EAN8", "EAN_8");
        this.mapTypes.put("CODE39", "CODE_39");
        this.mapTypes.put("CODE128", "CODE_128");
        this.mapTypes.put("EAN128", "CODE_128");
        //this.mapTypes.put(73, "EAN_14");
        this.mapTypes.put("UPCA", "UPC_A");
        this.mapTypes.put("UPCE0", "UPC_E");
        this.mapTypes.put("QRCODE", "QR_CODE");
        this.mapTypes.put("DATAMATRIX", "DATA_MATRIX");
        this.mapTypes.put("GS1_DATABAR", "RSS_14");
        if (scanner == null) {
            if ((deviceList != null) && (deviceList.size() != 0)) {
                scanner = barcodeManager.getDevice(deviceList.get(scannerIndex));
            } else {
                String errMsg = "Status: " + "Failed to get the specified scanner device! Please close and restart the application.";
                Log.e(TAG, errMsg);
                BaseScan.sendPluginResultError(currentCallbackContext, errMsg);
            }
            if (scanner != null) {
                scanner.addDataListener(this);
                scanner.addStatusListener(this);
                try {
                    scanner.enable();
                } catch (ScannerException e) {
                    Log.e(TAG, "Status: " + e.getMessage());
                    BaseScan.sendPluginResultError(currentCallbackContext, e.getMessage());
                }
            } else {
                String errMsg = "Status: " + "Failed to initialize the scanner device.";
                Log.e(TAG, errMsg);
                BaseScan.sendPluginResultError(currentCallbackContext, errMsg);
            }
        }
    }


    private void deInitScanner() {
        if (scanner != null) {
            try {
                scanner.cancelRead();
                scanner.disable();
            } catch (Exception e) {
                Log.e(TAG, "Status: " + e.getMessage());
            }
            try {
                scanner.removeDataListener(this);
                scanner.removeStatusListener(this);
            } catch (Exception e) {
                Log.e(TAG, "Error removing listeners: Status: " + e.getMessage());
            }
            try {
                scanner.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing scanner: Status: " + e.getMessage());
            }
            scanner = null;
        }
    }

    private void stopScan() {
        if (scanner != null) {
            try {
                // Reset continuous flag
                bContinuousMode = true;
                // Cancel the pending read.
                scanner.cancelRead();
            } catch (ScannerException e) {
                Log.e(TAG, "Error stopping scan: Status: " + e.getMessage());
            }
        }
    }

    @Override
    public void scan(CordovaInterface cordova, CordovaWebView webView, JSONArray args, final CallbackContext callbackContext) {
        this.currentCallbackContext = callbackContext;

        if (scanner == null) {
            initScanner();
        }
        if (scanner != null) {
            try {
                if (scanner.isEnabled()) {
                    if (scanner.isReadPending()) {
                        scanner.cancelRead();
                    }
                    // Submit a new read.
                    setSoftTrigger();
                    scanner.read();
                } else {
                    String msg = "Status: Scanner is not enabled";
                    Log.e(TAG, msg);
                    BaseScan.sendPluginResultError(currentCallbackContext, msg);
                }
            } catch (ScannerException e) {
                Log.e(TAG, "Status: " + e.getMessage());
                BaseScan.sendPluginResultError(currentCallbackContext, e.getMessage());
            }
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
        // De-initialize scanner
        deInitScanner();

        // Remove connection listener
        if (barcodeManager != null) {
            barcodeManager.removeConnectionListener(this);
            barcodeManager = null;
        }

        // Release all the resources
        if (emdkManager != null) {
            emdkManager.release();
            emdkManager = null;

        }
    }

    @Override
    public void onResume(boolean multitasking) {
        // The application is in foreground

        // Acquire the barcode manager resources
        if (emdkManager != null) {
            barcodeManager = (BarcodeManager) emdkManager.getInstance(FEATURE_TYPE.BARCODE);

            // Add connection listener
            if (barcodeManager != null) {
                barcodeManager.addConnectionListener(this);
            }

            // Enumerate scanner devices
            enumerateScannerDevices();

            // Initialize scanner
            initScanner();
            setTrigger();
            setDecoders();
        }
    }


    private void setTrigger() {
        if (scanner == null) {
            initScanner();
        }
        if (scanner != null) {
            //force to trigger hard scanner
            scanner.triggerType = TriggerType.HARD;
            /*switch (triggerIndex) {
                case 0: // Selected "HARD"
                    scanner.triggerType = TriggerType.HARD;
                    break;
                case 1: // Selected "SOFT"
                    scanner.triggerType = TriggerType.SOFT_ALWAYS;
                    break;
            }*/
        }
    }

    private void setSoftTrigger() {
        if (scanner == null) {
            initScanner();
        }
        if (scanner != null) {
            //force to trigger SOFT_ONCE scanner
            scanner.triggerType = TriggerType.SOFT_ONCE;
        }
    }


    //config barcode types allowed
    private void setDecoders() {
        if (scanner == null) {
            initScanner();
        }
        if ((scanner != null) && (scanner.isEnabled())) {
            try {
                ScannerConfig config = scanner.getConfig();
                config.decoderParams.ean8.enabled = true;
                config.decoderParams.ean13.enabled = true;
                config.decoderParams.qrCode.enabled = true;
                config.decoderParams.code39.enabled = true;
                config.decoderParams.code128.enabled = true;
                scanner.setConfig(config);
            } catch (ScannerException e) {
                Log.e(TAG, "Status: " + e.getMessage());
            }
        }
    }

    @Override
    public void onPause(boolean multitasking) {
        // The application is in background
        // De-initialize scanner
        deInitScanner();

        // Remove connection listener
        if (barcodeManager != null) {
            barcodeManager.removeConnectionListener(this);
            barcodeManager = null;
            deviceList = null;
        }

        // Release the barcode manager resources
        if (emdkManager != null) {
            emdkManager.release(FEATURE_TYPE.BARCODE);
        }
    }

    @Override
    public void onConnectionChange(ScannerInfo scannerInfo, ConnectionState connectionState) {

        String status;
        String scannerName = "";

        String statusExtScanner = connectionState.toString();
        String scannerNameExtScanner = scannerInfo.getFriendlyName();

        if (deviceList.size() != 0) {
            scannerName = deviceList.get(scannerIndex).getFriendlyName();
        }

        if (scannerName.equalsIgnoreCase(scannerNameExtScanner)) {

            switch (connectionState) {
                case CONNECTED:
                    deInitScanner();
                    initScanner();
                    setTrigger();
                    setDecoders();
                    break;
                case DISCONNECTED:
                    deInitScanner();
                    break;
            }
        } else {
            status = statusString + " " + scannerNameExtScanner + ":" + statusExtScanner;
        }
    }

    private void openScanner(EMDKManager emdkManager) throws Exception {
        this.emdkManager = emdkManager;

        // Acquire the barcode manager resources
        barcodeManager = (BarcodeManager) emdkManager.getInstance(FEATURE_TYPE.BARCODE);

        // Add connection listener
        if (barcodeManager != null) {
            barcodeManager.addConnectionListener(this);
        }

        // Enumerate scanner devices
        enumerateScannerDevices();

    }

    @Override
    public void onOpened(EMDKManager emdkManager) {
        try {
            this.openScanner(emdkManager);
            initScanner();
        } catch (Exception e) {
            BaseScan.sendPluginResultError(currentCallbackContext, e.getMessage());
        }
    }

    @Override
    public void onClosed() {
        if (emdkManager != null) {
            // Remove connection listener
            if (barcodeManager != null) {
                barcodeManager.removeConnectionListener(this);
                barcodeManager = null;
            }
            // Release all the resources
            emdkManager.release();
            emdkManager = null;
        }
        Log.e(TAG, "Status: " + "EMDK closed unexpectedly! Please close and restart the application.");
    }

    @Override
    public void onData(ScanDataCollection scanDataCollection) {

        if ((scanDataCollection != null) && (scanDataCollection.getResult() == ScannerResults.SUCCESS)) {
            ArrayList<ScanData> scanData = scanDataCollection.getScanData();
            //get last result
            ScanData data = scanData.get(scanData.size() - 1);
            ///for(ScanData data : scanData) {
            String dataString = data.getData();
            String parseType = "";
            try {
                String typeString = data.getLabelType().toString();
                parseType = mapTypes.get(typeString);
            } catch (Exception e) {
            }
            Log.d(TAG + " - Barcode: ", dataString);

            if (currentCallbackContext != null) {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("format", parseType);
                    obj.put("text", dataString);
                    BaseScan.sendPluginResultOK(currentCallbackContext, obj);
                } catch (Exception x) {
                    BaseScan.sendPluginResultError(currentCallbackContext, x.getMessage());
                }
            }
            //}
        } else {
            if (currentCallbackContext != null) {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("text", "");
                    PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                    result.setKeepCallback(true);
                    currentCallbackContext.sendPluginResult(result);
                } catch (Exception x) {
                    BaseScan.sendPluginResultError(currentCallbackContext, x.getMessage());
                }
            }
        }
    }

    @Override
    public void onStatus(StatusData statusData) {

        ScannerStates state = statusData.getState();
        switch (state) {
            case IDLE:
                statusString = statusData.getFriendlyName() + " is enabled and idle...";
                if (bContinuousMode) {
                    try {
                        // An attempt to use the scanner continuously and rapidly (with a delay < 100 ms between scans)
                        // may cause the scanner to pause momentarily before resuming the scanning.
                        // Hence add some delay (>= 100ms) before submitting the next read.
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        scanner.read();
                    } catch (ScannerException e) {
                        statusString = e.getMessage();
                    }
                }
                break;
            case WAITING:
                statusString = "Scanner is waiting for trigger press...";
                break;
            case SCANNING:
                statusString = "Scanning...";
                break;
            case DISABLED:
                statusString = statusData.getFriendlyName() + " is disabled.";
                break;
            case ERROR:
                statusString = "An error has occurred.";
                break;
            default:
                break;
        }
    }


    private void enumerateScannerDevices() {

        if (barcodeManager != null) {
            List<String> friendlyNameList = new ArrayList<String>();
            int spinnerIndex = 0;

            deviceList = barcodeManager.getSupportedDevicesInfo();

            if ((deviceList != null) && (deviceList.size() != 0)) {

                Iterator<ScannerInfo> it = deviceList.iterator();
                while (it.hasNext()) {
                    ScannerInfo scnInfo = it.next();
                    friendlyNameList.add(scnInfo.getFriendlyName());
                    if (scnInfo.isDefaultScanner()) {
                        defaultIndex = spinnerIndex;
                    }
                    ++spinnerIndex;
                }
            }
        }
    }

    /*private class ProcessProfileAsyncTask extends AsyncTask<String, Void, EMDKResults> {

        @Override
        protected EMDKResults doInBackground(String... params) {

            // Call processPrfoile with profile name, SET flag and config data to update the profile
            EMDKResults results = profileManager.processProfile(profileName, ProfileManager.PROFILE_FLAG.SET, params);

            return results;
        }

        @Override
        protected void onPostExecute(EMDKResults results) {

            super.onPostExecute(results);

            String resultString;

            //Check the return status of processProfile
            if(results.statusCode == EMDKResults.STATUS_CODE.SUCCESS) {

                resultString = "Profile update success.";

            }else {

                resultString = "Profile update failed.";
            }

        }
    } */

}

