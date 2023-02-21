  var exec = require('cordova/exec');

  var PLUGIN_NAME = 'BarcodeScan';

  var BarcodeScan = {
    scan: function (device, cb, error) {
      exec(cb, error, PLUGIN_NAME, 'scan', [device]);
    },
    enable: function (device, cb, error) {
      exec(cb, error, PLUGIN_NAME, 'enable', [device]);
    },
    getDevices: function (cb, error) {
      exec(cb, error, PLUGIN_NAME, 'getDevices', []);
    },
    launchAndroidSettings: function (pkg, cb, error) {
      exec(cb, error, PLUGIN_NAME, 'launchAndroidSettings', [pkg]);
    }
  };
  module.exports = BarcodeScan;