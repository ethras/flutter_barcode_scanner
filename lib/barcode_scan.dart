import 'dart:async';

import 'package:flutter/services.dart';

class Barcode {
  String value = "";
  String format = "";

  Barcode.fromMap(Map map) {
    value = map["value"];
    format = map["format"];
  }
}

class BarcodeScanner {
  static const CameraAccessDenied = 'PERMISSION_NOT_GRANTED';
  static const MethodChannel _channel =
      const MethodChannel('com.ethras.barcode_scan');

  static Future<List<Barcode>> scan({ScanOptions options}) async {
    if (options == null) {
      options = new ScanOptions();
    }
    final List list = await _channel.invokeMethod('scan', options.toMap());
    return list.map((map) => Barcode.fromMap(map)).toList();
  }
}

class ScanOptions {
  bool waitTap = false;

  Map<String, dynamic> toMap() {
    Map<String, dynamic> arguments = {'waitTap': waitTap};
    return arguments;
  }
}
