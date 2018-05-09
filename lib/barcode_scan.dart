import 'dart:async';

import 'package:flutter/services.dart';

class BarcodeScanner {
  static const CameraAccessDenied = 'PERMISSION_NOT_GRANTED';
  static const MethodChannel _channel =
      const MethodChannel('com.apptreesoftware.barcode_scan');
  static Future<String> scan({ScanOptions options}) async {
    if (options == null) {
      options = new ScanOptions();
    }
    return await _channel.invokeMethod('scan', options.toMap());
  }
}

class ScanOptions {
  bool waitTap = false;

  Map<String, dynamic> toMap() {
    Map<String, dynamic> arguments = {'waitTap': waitTap};
    return arguments;
  }
}
