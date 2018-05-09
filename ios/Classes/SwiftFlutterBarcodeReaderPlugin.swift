import Flutter
import UIKit
    
public class SwiftFlutterBarcodeReaderPlugin: NSObject, FlutterPlugin, BarcodeScannerViewControllerDelegate {
    private var result: FlutterResult!
    private var _hostViewController: UIViewController!
    private var _navigationViewController: UINavigationController!
    private var _scanOptions: ScanOptions!
    
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "com.apptreesoftware.barcode_scan", binaryMessenger: registrar.messenger())
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
        showBarcodeView()
    }
    else {
        result(FlutterMethodNotImplemented)
    }
    
  }
    func showBarcodeView() {
        let scannerViewController = BarcodeScannerViewController(options: _scanOptions)
        let navigationController = UINavigationController(rootViewController: scannerViewController as? UIViewController ?? UIViewController())
        scannerViewController.delegate = self
        _hostViewController.present(navigationController, animated: false, completion: nil)
    }
    
    func barcodeScannerViewController(_ controller: BarcodeScannerViewController, didScanBarcodeWithResult result: String) {
        if (self.result != nil) {
            self.result(result)
        }
    }
    
    func barcodeScannerViewController(_ controller: BarcodeScannerViewController, didFailWithErrorCode errorCode: String) {
        if (self.result != nil)  {
            result(FlutterError(code: errorCode, message: nil, details: nil))
        }
    }
}

class ScanOptions {
    var waitTap: Bool = false
}
