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

//-----------------------
// Format

enum BarcodeFormat {
  AllFormats,
  QrCode,
}

String barcodeFormatToString(BarcodeFormat format) {
  switch (format) {
    case BarcodeFormat.AllFormats:
      return "ALL_FORMATS";
    case BarcodeFormat.QrCode:
      return "QR_CODE";
  }
  return "ALL_FORMATS";
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
  List<BarcodeFormat> formats = [BarcodeFormat.AllFormats];

  Map<String, dynamic> toMap() {
    Map<String, dynamic> arguments = {
      'waitTap': waitTap,
      'formats':
          formats.map((format) => barcodeFormatToString(format)).toList(),
    };
    return arguments;
  }
}
