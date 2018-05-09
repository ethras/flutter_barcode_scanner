package com.ethras.barcodescan.barcode

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Camera
import android.util.DisplayMetrics

import com.ethras.barcodescan.ui.CameraSource
import com.ethras.barcodescan.util.AbstractCaptureActivity
import com.ethras.barcodescan.util.MobileVisionException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector

import java.util.ArrayList

class BarcodeCaptureActivity : AbstractCaptureActivity<BarcodeGraphic>(), BarcodeUpdateListener {

    @SuppressLint("InlinedApi")
    @Throws(MobileVisionException::class)
    override fun createCameraSource() {
        val context = applicationContext

        val barcodeDetector = BarcodeDetector.Builder(context)
                .setBarcodeFormats(intent.getIntExtra(AbstractCaptureActivity.FORMATS, Barcode.ALL_FORMATS))
                .build()

        val barcodeTrackerFactory = BarcodeTrackerFactory(graphicOverlay,
                this, showText)

        barcodeDetector.setProcessor(
                MultiProcessor.Builder(barcodeTrackerFactory).build())

        if (!barcodeDetector.isOperational) {
            val lowStorageFilter = IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW)
            val hasLowStorage = registerReceiver(null, lowStorageFilter) != null

            if (hasLowStorage) {
                throw MobileVisionException("Low Storage.")
            }
        }

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)

        cameraSource = CameraSource.Builder(applicationContext, barcodeDetector)
                .setFacing(camera)
                .setRequestedPreviewSize(metrics.heightPixels, metrics.widthPixels)
                .setFocusMode(if (autoFocus) Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE else "")
                .setFlashMode(if (useFlash) Camera.Parameters.FLASH_MODE_TORCH else "")
                .setRequestedFps(fps)
                .build()
    }

    override fun onTap(rawX: Float, rawY: Float): Boolean {
        if (!waitTap) {
            return false
        }

        val list = ArrayList<Barcode>()

        if (multiple) {
            for (graphic in graphicOverlay.graphics) {
                list.add(graphic.barcode!!)
            }
        } else {
            val graphic = graphicOverlay.getBest(rawX, rawY)
            if (graphic?.barcode != null) {
                list.add(graphic.barcode!!)
            }
        }

        if (!list.isEmpty()) {
            success(list)
            return true
        }

        return false
    }

    override fun onBarcodeDetected(barcode: Barcode) {
        if (!waitTap) {
            val list = ArrayList<Barcode>(1)
            list.add(barcode)
            success(list)
        }
    }

    private fun success(list: ArrayList<Barcode>) {
        val data = Intent()
        data.putExtra(AbstractCaptureActivity.OBJECT, list)
        setResult(CommonStatusCodes.SUCCESS, data)
        finish()
    }
}
