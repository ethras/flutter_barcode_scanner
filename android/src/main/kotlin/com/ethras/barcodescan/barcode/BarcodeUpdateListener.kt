package com.ethras.barcodescan.barcode


import androidx.annotation.UiThread
import com.google.android.gms.vision.barcode.Barcode

interface BarcodeUpdateListener {

    @UiThread
    fun onBarcodeDetected(barcode: Barcode)

}
