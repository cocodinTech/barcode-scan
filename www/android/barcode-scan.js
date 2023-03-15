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
    launchAndroidSettings: function (param, cb, error) {
      exec(cb, error, PLUGIN_NAME, 'launchAndroidSettings', [param]);
    },
    play: function (filename, vol, cb, error) {
      exec(cb, error, PLUGIN_NAME, 'play', [filename, vol]);
    }
  };
  module.exports = BarcodeScan;