//
//  CodeFrameView.swift
//  barcode_scan
//
//  Created by Vladimir on 09.05.18.
//

import Foundation
import AVFoundation


class CodeFrameView: UIButton {
    var code: AVMetadataMachineReadableCodeObject!
    
    init() {
        super.init(frame: CGRect.zero)
        layer.borderColor = UIColor.green.cgColor
        layer.borderWidth = 2
        backgroundColor = UIColor.green
        isUserInteractionEnabled = true
        isEnabled = true
        translatesAutoresizingMaskIntoConstraints = false
        alpha = 0.3
        
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    func setPrimary() {
        layer.borderColor = UIColor.red.cgColor
    }
    
    func setCode(code: AVMetadataMachineReadableCodeObject) {
        self.code = code
        var tmpBounds = code.bounds
        if (tmpBounds.height < CGFloat(5)) {
            tmpBounds.size.height = CGFloat(20)
        }
        frame = tmpBounds
    }
    
    func reset() {
        layer.borderColor = UIColor.green.cgColor
        frame = CGRect.zero
        code = nil
    }
}
