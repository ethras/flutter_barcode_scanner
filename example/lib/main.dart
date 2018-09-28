import 'dart:async';

import 'package:barcode_scanner/barcode_scan.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String barcode = "";
  ScanOptions scanOptions = ScanOptions();

  @override
  initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
          appBar: AppBar(
            title: Text('Barcode Scanner Example'),
          ),
          body: Center(
            child: Column(
              children: <Widget>[
                DropdownButton(
                    value: scanOptions.formats[0],
                    items: BarcodeFormat.values.map((format) {
                      return DropdownMenuItem<BarcodeFormat>(
                          child: Text(format.toString()), value: format);
                    }).toList(),
                    onChanged: (format) {
                      setState(() {
                        scanOptions.formats = [format];
                      });
                    }),
                SwitchListTile(
                  title: Text("Wait a tap to capture"),
                  value: scanOptions.waitTap,
                  onChanged: (value) => setState(() {
                        scanOptions.waitTap = !scanOptions.waitTap;
                      }),
                ),
                Container(
                  child: MaterialButton(onPressed: scan, child: Text("Scan")),
                  padding: const EdgeInsets.all(8.0),
                ),
                Text(barcode),
              ],
            ),
          )),
    );
  }

  Future scan() async {
    try {
      var barcodes = await BarcodeScanner.scan(options: scanOptions);
      setState(() => this.barcode = barcodes.first.value);
    } on PlatformException catch (e) {
      if (e.code == BarcodeScanner.CameraAccessDenied) {
        setState(() {
          this.barcode = 'The user did not grant the camera permission!';
        });
      } else {
        setState(() => this.barcode = 'Unknown error: $e');
      }
    } on FormatException {
      setState(() => this.barcode =
          'null (User returned using the "back"-button before scanning anything. Result)');
    } catch (e) {
      setState(() => this.barcode = 'Unknown error: $e');
    }
  }
}
