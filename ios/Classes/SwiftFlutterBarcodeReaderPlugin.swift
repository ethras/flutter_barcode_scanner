import Flutter
import UIKit
import AVFoundation

public class SwiftFlutterBarcodeReaderPlugin: NSObject, FlutterPlugin, BarcodeScannerViewControllerDelegate {
    private var result: FlutterResult!
    private var _hostViewController: UIViewController!
    private var _navigationViewController: UINavigationController!
    private var _scanOptions: ScanOptions!

    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "com.ethras.barcode_scan", binaryMessenger: registrar.messenger())
        let instance = SwiftFlutterBarcodeReaderPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
        instance._hostViewController = UIApplication.shared.delegate?.window??.rootViewController
        instance._scanOptions = ScanOptions()
    }

    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        if ("scan" == call.method) {
            self.result = result
            let dic = call.arguments as? [String: Any]
            _scanOptions.waitTap = dic?["waitTap"] as! Bool
            let formats = dic?["formats"] as! [String]

            if (formats.count == 1 && formats[0] == "ALL_FORMATS") {
                _scanOptions.formats = []
            } else {
                _scanOptions.formats = formats.compactMap { format -> AVMetadataObject.ObjectType? in
                    getObjectType(formatString: format)
                }
            }

            showBarcodeView()
        } else {
            result(FlutterMethodNotImplemented)
        }

    }

    func showBarcodeView() {
        let scannerViewController = BarcodeScannerViewController(options: _scanOptions)
        let navigationController = UINavigationController(rootViewController: scannerViewController as UIViewController)
        scannerViewController.delegate = self
        _hostViewController.present(navigationController, animated: false, completion: nil)
    }

    func getObjectType(formatString: String) -> AVMetadataObject.ObjectType? {
        switch formatString {
        case "QR_CODE":
            return AVMetadataObject.ObjectType.qr
        default:
            return nil
        }
    }

    func getTypeStringFormat(code: AVMetadataMachineReadableCodeObject) -> String {
        let type: String = {
            switch code.type {
            case AVMetadataObject.ObjectType.aztec:
                return "AZTEC"
            case AVMetadataObject.ObjectType.code128:
                return "CODE_128"
            case AVMetadataObject.ObjectType.code39:
                return "CODE_39"
            case AVMetadataObject.ObjectType.code39Mod43:
                return "CODE_39_MOD_43"
            case AVMetadataObject.ObjectType.code93:
                return "CODE_93"
            case AVMetadataObject.ObjectType.dataMatrix:
                return "DATA_MATRIX"
            case AVMetadataObject.ObjectType.ean13:
                return "EAN_13"
            case AVMetadataObject.ObjectType.ean8:
                return "EAN_8"
            case AVMetadataObject.ObjectType.itf14:
                return "ITF"
            case AVMetadataObject.ObjectType.pdf417:
                return "PDF418"
            case AVMetadataObject.ObjectType.qr:
                return "QR_CODE"
            case AVMetadataObject.ObjectType.upce:
                return "UPC_E"
            default:
                return code.type.rawValue
            }
        }()
        return type
    }

    func barcodeScannerViewController(_ controller: BarcodeScannerViewController, didScanBarcodeWithResult result: [AVMetadataMachineReadableCodeObject]) {
        if (self.result != nil) {
            var array: [[String: String]] = []
            for code in result {
                let map = ["value": code.stringValue ?? "", "format": getTypeStringFormat(code: code)]
                array.append(map)
            }
            self.result(array)
        }
    }

    func barcodeScannerViewController(_ controller: BarcodeScannerViewController, didFailWithErrorCode errorCode: String) {
        if (self.result != nil) {
            result(FlutterError(code: errorCode, message: nil, details: nil))
        }
    }
}

class ScanOptions {
    var waitTap: Bool = false
    var formats: [AVMetadataObject.ObjectType] = []
}
