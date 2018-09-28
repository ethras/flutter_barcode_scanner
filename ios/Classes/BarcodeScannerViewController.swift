
import Foundation
import UIKit
import MTBBarcodeScanner

class BarcodeScannerViewController: UIViewController, UIGestureRecognizerDelegate {
    lazy private var previewView: UIView = UIView.init(frame: view.bounds)
    lazy private var scanner: MTBBarcodeScanner = MTBBarcodeScanner(previewView: previewView)
    public weak var delegate: BarcodeScannerViewControllerDelegate?
    private var tap: UITapGestureRecognizer!
    
    private var codeFrameViews: [CodeFrameView]!
    
    private var _codes: [AVMetadataMachineReadableCodeObject]!
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
        previewView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(previewView)
        
        // Navigation item
        navigationItem.leftBarButtonItem = UIBarButtonItem(barButtonSystemItem: .cancel, target: self, action: #selector(self.cancel))
        if hasTorch {
            navigationItem.rightBarButtonItem = UIBarButtonItem(title: "Flash On", style: .plain, target: self, action: #selector(self.toggle))
        }
        
        codeFrameViews = []
        for _ in 1...10 {
            let codeView = CodeFrameView()
            if (scanOptions.waitTap) {
                codeView.addTarget(self, action: #selector(self.tapToScan), for: .touchUpInside)
            }
            codeFrameViews.append(codeView)
            self.view.addSubview(codeView)
            self.view.bringSubview(toFront: codeView)
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
    
    
    private func validateScan(code: AVMetadataMachineReadableCodeObject) {
        self.delegate?.barcodeScannerViewController(self, didScanBarcodeWithResult: _codes)
            self.dismiss(animated: false, completion: nil)
    }
    
    @objc func tapToScan(sender: UIButton!) {
        print("Tapped ")
        let codeView = sender as! CodeFrameView
        let code = codeView.code!
        validateScan(code: code)
    }
    
    func startScan() {
        do {
            try self.scanner.startScanning(resultBlock: { codes in
                if let codes = codes {
                    self._codes = codes
                    
                    // Remove all frame from superview
                    for codeView in self.codeFrameViews {
                        codeView.reset()
                    }
                    
                    var i = 0
                    for code in codes {
                        // Filter by wanted formats
                        if (self.scanOptions.formats.contains(code.type) || self.scanOptions.formats.isEmpty){
                            if i < self.codeFrameViews.count {
                                self.codeFrameViews[0].setPrimary()
                                self.codeFrameViews[i].setCode(code: code)
                                i += 1
                                if (!self.scanOptions.waitTap) {
                                    self.validateScan(code: code)
                                }
                            }
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
