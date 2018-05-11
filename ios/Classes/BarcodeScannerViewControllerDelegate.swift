//
//  BarcodeScannerViewControllerDelegate.swift
//  flutter_barcode_reader
//
//  Created by Vladimir on 08.05.18.
//

import Foundation
import AVFoundation

protocol BarcodeScannerViewControllerDelegate: class {
    func barcodeScannerViewController(_ controller: BarcodeScannerViewController, didScanBarcodeWithResult result: [AVMetadataMachineReadableCodeObject])
    func barcodeScannerViewController(_ controller: BarcodeScannerViewController, didFailWithErrorCode errorCode: String)
}
