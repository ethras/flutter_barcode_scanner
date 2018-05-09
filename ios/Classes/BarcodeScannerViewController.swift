
import Foundation
import UIKit
import MTBBarcodeScanner

class BarcodeScannerViewController: UIViewController, UIGestureRecognizerDelegate {
    lazy private var previewView: UIView = UIView.init(frame: view.bounds)
    lazy private var scanner: MTBBarcodeScanner = MTBBarcodeScanner(previewView: previewView)
    public weak var delegate: BarcodeScannerViewControllerDelegate?
    private var codeFrameView: UIView!
    
    private var currentCode: String?
    private var scanOptions: ScanOptions!
    
    init(options: ScanOptions) {
        super.init(nibName: nil , bundle: nil)
        scanOptions = options
        
        if (scanOptions.waitTap) {
            print("Tap to scan")
        }
        else {
            print("Scanning ASAP")
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        previewView.frame = view.bounds
        view.addSubview(previewView)
        
        codeFrameView = UIView()
        
        // Enable or disable scan on tap
        if (scanOptions.waitTap) {
            let tap = UITapGestureRecognizer(target: self, action: #selector(self.tapToScan(_:)))
            tap.delegate = self
            codeFrameView.addGestureRecognizer(tap)
        }
        
        if let qrCodeFrameView = codeFrameView {
            qrCodeFrameView.layer.borderColor = UIColor.green.cgColor
            qrCodeFrameView.layer.borderWidth = 2
            view.addSubview(qrCodeFrameView)
            view.bringSubview(toFront: qrCodeFrameView)
        }
        
        // Navigation item
        navigationItem.leftBarButtonItem = UIBarButtonItem(barButtonSystemItem: .cancel, target: self, action: #selector(self.cancel))
        if hasTorch {
            navigationItem.rightBarButtonItem = UIBarButtonItem(title: "Flash On", style: .plain, target: self, action: #selector(self.toggle))
        }
    }

    override func viewWillTransition(to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator) {
        super.viewWillTransition(to: size, with: coordinator)
            self.previewView.frame = CGRect(x: 0.0, y: 0.0, width: self.view.frame.size.height, height: self.view.frame.size.width)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        if scanner.isScanning() {
            scanner.stopScanning()
        }
        MTBBarcodeScanner.requestCameraPermission(success: {(_ success: Bool) -> Void in
            if success {
                self.startScan()
            }
            else {
                self.delegate?.barcodeScannerViewController(self, didFailWithErrorCode: "PERMISSION_NOT_GRANTED")
                self.dismiss(animated: false, completion: nil)
            }
        })
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        if isFlashOn() {
            toggleFlash(false)
        }
    }
    
    @objc func cancel() {
        dismiss(animated: true, completion: nil)
    }
    
    @objc func tapToScan(_ gestureRecognizer: UITapGestureRecognizer) {
        print("Tapped")
        validateScan()
    }
    
    private func validateScan() {
        if let currentCode = currentCode {
            self.delegate?.barcodeScannerViewController(self, didScanBarcodeWithResult: currentCode)
            self.dismiss(animated: false, completion: nil)
        }
    }
    
    func startScan() {
        do {
            try self.scanner.startScanning(resultBlock: { codes in
                if let codes = codes {
                    if codes.count == 0 {
                        self.codeFrameView.frame = CGRect.zero
                    }
                    for code in codes {
                        let stringValue = code.stringValue!
                        print("Found code: \(stringValue)")
                        var bounds = code.bounds
                        if (code.bounds.height < CGFloat(5)) {
                            var tmp = code.bounds
                            tmp.size.height = CGFloat(20)
                            bounds = tmp
                        }
                
                        self.codeFrameView.frame = bounds
                        self.currentCode = stringValue
                        
                        if (!self.scanOptions.waitTap) {
                            self.validateScan()
                        }
                    }
                }
            })
        } catch {
            NSLog("Unable to start scanning")
        }
    }
    
    func updateFlashButton() {
        if !hasTorch {
            return
        }
        if isFlashOn() {
            navigationItem.rightBarButtonItem = UIBarButtonItem(title: "Flash Off", style: .plain, target: self, action: #selector(self.toggle))
        }
        else {
            navigationItem.rightBarButtonItem = UIBarButtonItem(title: "Flash On", style: .plain, target: self, action: #selector(self.toggle))
        }
    }
    
    @objc func toggle() {
        toggleFlash(!isFlashOn())
        updateFlashButton()
    }
    
    func isFlashOn() -> Bool {
        let device: AVCaptureDevice? = AVCaptureDevice.default(for: .video)
        if device != nil {
            return device?.torchMode == .on || device?.torchMode == AVCaptureDevice.TorchMode.on
        }
        return false
    }
    
    var hasTorch: Bool {
        let device: AVCaptureDevice? = AVCaptureDevice.default(for: .video)
        if device != nil {
            return device?.hasTorch ?? false
        }
        return false
    }
    
    func toggleFlash(_ on: Bool) {
        let device: AVCaptureDevice? = AVCaptureDevice.default(for: .video)
        if device == nil {
            return
        }
    
        if (device?.hasFlash)! && (device?.hasTorch)! {
            try? device?.lockForConfiguration()
            if on {
                device?.flashMode = .on
                device?.torchMode = .on
            }
            else {
                device?.flashMode = .off
                device?.torchMode = .off
            }
            device?.unlockForConfiguration()
        }
    }
    
    
}
