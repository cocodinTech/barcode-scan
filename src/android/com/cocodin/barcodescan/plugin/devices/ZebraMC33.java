package com.cocodin.barcodescan.plugin.devices;

import static com.symbol.emdk.EMDKManager.FEATURE_TYPE.BARCODE;
import static com.symbol.emdk.barcode.Scanner.TriggerType.HARD;
import static com.symbol.emdk.barcode.Scanner.TriggerType.SOFT_ONCE;

import android.util.Log;

import com.cocodin.barcodescan.plugin.BaseScan;
import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.BarcodeManager.ConnectionState;
import com.symbol.emdk.barcode.BarcodeManager.ScannerConnectionListener;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.ScanDataCollection.ScanData;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.Scanner.DataListener;
import com.symbol.emdk.barcode.Scanner.StatusListener;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by alberto.doval on 21/05/18.
 */

public class ZebraMC33 extends BaseScan implements EMDKListener, DataListener, StatusListener, ScannerConnectionListener {

  private static final String TAG = "ZebraMC33";
  private EMDKManager emdkManager = null;
  private BarcodeManager barcodeManager = null;
  private Scanner scanner = null;
  private int scannerIndex = -1; // Keep the selected scanner
  private String statusString = "";
  private boolean bSoftTriggerSelected = false;
  private final Map<String, String> mapTypes = new HashMap<>();
  private boolean isSetDecodders = false;
  private boolean initDimZebraDecos = false;


  public ZebraMC33(CordovaInterface cordova, CordovaWebView webView) {
    super(cordova, webView);
    initialize(cordova, webView);
  }

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    EMDKResults results = EMDKManager.getEMDKManager(cordova.getContext(), this);
    if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
      Log.e(TAG, "Status: " + "EMDKManager object request failed!");
    }
  }

  @Override
  public String getDeviceName() {
    return TAG;
  }

  public void enable(CordovaInterface cordova, CordovaWebView webView, JSONArray args, final CallbackContext callbackContext) {
    try {
      try {
        this.initDimZebraDecos = (boolean) args.get(1);
      } catch (Exception e) {
        Log.e(TAG, Objects.requireNonNull(e.getMessage()));
      }
      this.currentCallbackContext = callbackContext;
      initBarcodeManager();
      JSONObject obj = new JSONObject();
      PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
      result.setKeepCallback(true);
      callbackContext.sendPluginResult(result);
    } catch (Exception e) {
      Log.e(TAG, Objects.requireNonNull(e.getMessage()));
      BaseScan.sendPluginResultError(currentCallbackContext, e.getMessage());
    }
  }

  @Override
  public void scan(CordovaInterface cordova, CordovaWebView webView, JSONArray args, CallbackContext callbackContext) {
    this.currentCallbackContext = callbackContext;
    if (scanner != null) {
      try {
        bSoftTriggerSelected = true;
        scanner.cancelRead();
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
    initBarcodeManager();
  }

  @Override
  public void onPause(boolean multitasking) {
    deInitScanner();
    // Remove connection listener
    if (barcodeManager != null) {
      barcodeManager.removeConnectionListener(this);
      barcodeManager = null;
    }
    // Release the barcode manager resources
    if (emdkManager != null) {
      emdkManager.release(BARCODE);
    }
  }

  @Override
  public void onOpened(EMDKManager emdkManager) {
    this.emdkManager = emdkManager;
  }

  @Override
  public void onClosed() {
  }

  @Override
  public void onConnectionChange(ScannerInfo scannerInfo, ConnectionState connectionState) {
  }

  @Override
  public void onData(ScanDataCollection scanDataCollection) {
    if ((scanDataCollection != null) && (scanDataCollection.getResult() == ScannerResults.SUCCESS)) {
      ArrayList<ScanDataCollection.ScanData> scanData = scanDataCollection.getScanData();
      //get last result
      ScanData data = scanData.get(scanData.size() - 1);
      ///for(ScanData data : scanData) {
      String dataString = data.getData();
      String parseType = "";
      try {
        String typeString = data.getLabelType().toString();
        parseType = mapTypes.get(typeString);
      } catch (Exception ignored) {
      }
      Log.d(TAG + " - Barcode: ", dataString);
      if (currentCallbackContext != null) {
        try {
          JSONObject obj = new JSONObject();
          obj.put("format", parseType);
          obj.put("text", dataString);
          BaseScan.sendPluginResultOK(currentCallbackContext, obj);
        } catch (Exception e) {
          BaseScan.sendPluginResultError(currentCallbackContext, e.getMessage());
        }
      }
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
        // set trigger type
        if (bSoftTriggerSelected) {
          scanner.triggerType = SOFT_ONCE;
          bSoftTriggerSelected = false;
        } else {
          scanner.triggerType = HARD;
        }
        // submit read
        if (!scanner.isReadPending()) {
          if (!isSetDecodders) {
            if (initDimZebraDecos) {
              setDynamicDecoders();
            } else {
              setDecoders();
            }
          }
          try {
            scanner.read();
          } catch (ScannerException e) {
            Log.i(TAG, Objects.requireNonNull(e.getMessage()));
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

  private void initBarcodeManager() {
    barcodeManager = (BarcodeManager) emdkManager.getInstance(BARCODE);
    // Add connection listener
    if (barcodeManager != null) {
      barcodeManager.addConnectionListener(this);
      initCodeTypes();
      initScanner();
    }
  }

  private void initScanner() {
    if (scanner != null) {
      deInitScanner();
    }
    List<ScannerInfo> deviceList = barcodeManager.getSupportedDevicesInfo();
    if ((deviceList != null) && (!deviceList.isEmpty())) {
      if (barcodeManager != null) {
        set2DImagerScanner(deviceList);
        scanner = barcodeManager.getDevice(deviceList.get(scannerIndex));
      }
    } else {
      this.throwNewException("Status: No supported devices info");
    }
    if (scanner != null) {
      scanner.addDataListener(this);
      scanner.addStatusListener(this);
      try {
        scanner.enable();
      } catch (ScannerException e) {
        deInitScanner();
        Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        BaseScan.sendPluginResultError(currentCallbackContext, e.getMessage());
      }
    }
  }

  private void deInitScanner() {
    if (scanner != null) {
      try {
        scanner.disable();
      } catch (Exception e) {
        Log.e(TAG, "Status: " + e.getMessage());
      }
      try {
        scanner.removeDataListener(this);
        scanner.removeStatusListener(this);
      } catch (Exception e) {
        Log.e(TAG, "Status: " + e.getMessage());
      }
      try {
        scanner.release();
      } catch (Exception e) {
        Log.e(TAG, "Status: " + e.getMessage());
      }
      scanner = null;
    }
  }

  private void set2DImagerScanner(List<ScannerInfo> deviceList) {
    int index = 0;
    for (ScannerInfo scnInfo : deviceList) {
      if (scnInfo.getFriendlyName().equals("2D Barcode Imager")) {
        scannerIndex = index;
      }
      index++;
    }
  }


  private void initCodeTypes() {
    this.mapTypes.clear();
    this.mapTypes.put("EAN13", "EAN_13");
    this.mapTypes.put("EAN8", "EAN_8");
    this.mapTypes.put("CODE39", "CODE_39");
    this.mapTypes.put("CODE128", "CODE_128");
    this.mapTypes.put("EAN128", "CODE_128");
    this.mapTypes.put("UPCA", "UPC_A");
    this.mapTypes.put("UPCE0", "UPC_E");
    this.mapTypes.put("UPCE1", "UPC_E_COMPUESTO");
    this.mapTypes.put("QRCODE", "QR_CODE");
    this.mapTypes.put("DATAMATRIX", "DATA_MATRIX");
    this.mapTypes.put("GS1_DATABAR", "RSS_14");
    this.mapTypes.put("I2OF5", "I2OF5");
  }

  private void setDecoders() {
    if ((scanner != null) && (scanner.isEnabled())) {
      try {
        ScannerConfig config = scanner.getConfig();
        config.decoderParams.ean8.enabled = true;
        config.decoderParams.ean13.enabled = true;
        config.decoderParams.qrCode.enabled = true;
        config.decoderParams.code39.enabled = true;
        config.decoderParams.code128.enabled = true;
        config.decoderParams.dataMatrix.enabled = true;
        config.decoderParams.gs1Databar.enabled = true;
        config.decoderParams.upce0.enabled = true;
        config.decoderParams.upca.enabled = true;
        config.decoderParams.i2of5.enabled = true;
        /* config.decoderParams.chinese2of5.enabled = true;
        config.decoderParams.matrix2of5.enabled = true;
        config.decoderParams.australianPostal.enabled = true;
        config.decoderParams.aztec.enabled = true;
        config.decoderParams.canadianPostal.enabled = true;
        config.decoderParams.codaBar.enabled = true; */
        scanner.setConfig(config);
        isSetDecodders = true;
        Log.e(TAG, "decoderParams: " + config.decoderParams);
      } catch (ScannerException e) {
        Log.e(TAG, "Status: " + e.getMessage());
      }
    }
  }

  private void setDynamicDecoders() {
    if (scanner == null || !scanner.isEnabled()) {
      return;
    }
    try {
      ScannerConfig config = scanner.getConfig();
      Object decoderParams = config.decoderParams;
      // Verificar la clase de decoderParams y sus campos
      Log.i(TAG, "Clase de decoderParams: " + decoderParams.getClass().getName());
      Field[] decoderFields = decoderParams.getClass().getDeclaredFields();
      for (Field decoderField : decoderFields) {
        decoderField.setAccessible(true);
        Object decoderConfig = decoderField.get(decoderParams);
        if (decoderConfig == null) {
          Log.i(TAG, "Campo " + decoderField.getName() + " es null");
          continue;
        }
        // Verificar la clase del decoderConfig y sus campos
        Log.i(TAG, "Clase de " + decoderField.getName() + ": " + decoderConfig.getClass().getName());
        Field[] configFields = decoderConfig.getClass().getDeclaredFields();
        for (Field configField : configFields) {
          Log.i(TAG, "Campo en " + decoderField.getName() + ": " + configField.getName());
        }
        // Buscar el campo "enabled" en la jerarquía de clases
        Class<?> clazz = decoderConfig.getClass();
        boolean enabledFound = false;
        while (clazz != null && !enabledFound) {
          try {
            Field enabledField = clazz.getDeclaredField("enabled");
            enabledField.setAccessible(true);
            enabledField.setBoolean(decoderConfig, true);
            Log.i(TAG, "Decoder habilitado: " + decoderField.getName());
            enabledFound = true;
          } catch (NoSuchFieldException e) {
            clazz = clazz.getSuperclass(); // Buscar en la superclase
          }
        }
        if (!enabledFound) {
          Log.i(TAG, "Decoder " + decoderField.getName() + " no tiene campo 'enabled'");
        }
      }
      scanner.setConfig(config);
      isSetDecodders = true;
    } catch (ScannerException | IllegalAccessException e) {
      Log.e(TAG, "Error: " + e.getMessage());
    }
  }

  private void throwNewException(String msg) {
    throw new RuntimeException(new Exception(msg));
  }


}
