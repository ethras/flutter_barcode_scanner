//
//  CodeFrameView.swift
//  barcode_scan
//
//  Created by Vladimir on 09.05.18.
//

import Foundation
import AVFoundation

class CodeFrameView: UIView {
    private var code: AVMetadataMachineReadableCodeObject!
    
    init(code: AVMetadataMachineReadableCodeObject) {
        self.code = code
        super.init(frame: code.bounds)
        
    }
    
    
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
}
