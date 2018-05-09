//
//  BarcodeScannerViewControllerDelegate.swift
//  flutter_barcode_reader
//
//  Created by Vladimir on 08.05.18.
//

import Foundation

protocol BarcodeScannerViewControllerDelegate: class {
    func barcodeScannerViewController(_ controller: BarcodeScannerViewController, didScanBarcodeWithResult result: String)
    func barcodeScannerViewController(_ controller: BarcodeScannerViewController, didFailWithErrorCode errorCode: String)
}

protocol CodeFrameViewDelegate: class {
    func codeFrameView(_ view: CodeFrameView, result: String)
}

