
import Foundation
import UIKit
import MTBBarcodeScanner

class BarcodeScannerViewController: UIViewController, UIGestureRecognizerDelegate {
    lazy private var previewView: UIView = UIView.init(frame: view.bounds)
    lazy private var scanner: MTBBarcodeScanner = MTBBarcodeScanner(previewView: previewView)
    public weak var delegate: BarcodeScannerViewControllerDelegate?
    private var qrCodeFrameView: UIView!
    
    private var currentCode: String?
    
    init() {
        super.init(nibName: nil , bundle: nil)
        
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        previewView.frame = view.bounds
        //previewView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(previewView)
        
        qrCodeFrameView = UIView()
        
        let tap = UITapGestureRecognizer(target: self, action: #selector(self.tapToScan(_:)))
        tap.delegate = self
        qrCodeFrameView.addGestureRecognizer(tap)
        
        if let qrCodeFrameView = qrCodeFrameView {
            qrCodeFrameView.layer.borderColor = UIColor.green.cgColor
            qrCodeFrameView.layer.borderWidth = 2
            view.addSubview(qrCodeFrameView)
            view.bringSubview(toFront: qrCodeFrameView)
        }
        
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
                        self.qrCodeFrameView.frame = CGRect.zero
                    }
                    for code in codes {
                        let stringValue = code.stringValue!
                        print("Found code: \(stringValue)")
                    
                        self.qrCodeFrameView.frame = code.bounds
                        self.currentCode = stringValue
//                        self.qrCodeFrameView.bounds = code.bounds
//                        self.delegate?.barcodeScannerViewController(self, didScanBarcodeWithResult: code.stringValue!)
//                        self.dismiss(animated: false, completion: nil)
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
