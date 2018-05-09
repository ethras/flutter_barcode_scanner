package com.ethras.barcodescan

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat

import com.ethras.barcodescan.barcode.BarcodeCaptureActivity
import com.ethras.barcodescan.ui.CameraSource
import com.ethras.barcodescan.util.AbstractCaptureActivity
import com.ethras.barcodescan.util.MobileVisionException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.vision.barcode.Barcode

import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.util.*

/**
 * FlutterMobileVisionPlugin
 */
class BarcodeScanPlugin private constructor(private val activity: Activity) : MethodCallHandler, PluginRegistry.ActivityResultListener, PluginRegistry.RequestPermissionsResultListener {
    private var result: Result? = null

    private var useFlash = false
    private var autoFocus = true
    private var formats = Barcode.ALL_FORMATS
    private var multiple = false
    private var waitTap = false
    private var showText = false
    private var camera = CameraSource.CAMERA_FACING_BACK
    private var fps = 15.0f

    override fun onMethodCall(call: MethodCall, result: Result) {

        val arguments = call.arguments<Map<String, Any>>()

        this.result = result

        if (arguments.containsKey("flash")) {
            useFlash = arguments["flash"] as Boolean
        }

        if (arguments.containsKey("autoFocus")) {
            autoFocus = arguments["autoFocus"] as Boolean
        }

        if (arguments.containsKey("formats")) {
            formats = arguments["formats"] as Int
        }

        if (arguments.containsKey("multiple")) {
            multiple = arguments["multiple"] as Boolean
        }

        if (arguments.containsKey("waitTap")) {
            waitTap = arguments["waitTap"] as Boolean
        }

        if (multiple) {
            waitTap = true
        }

        if (arguments.containsKey("showText")) {
            showText = arguments["showText"] as Boolean
        }

        if (arguments.containsKey("camera")) {
            camera = arguments["camera"] as Int
        }

        if (arguments.containsKey("fps")) {
            val tfps = arguments["fps"] as Double
            fps = tfps.toFloat()
        }

        var rc = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
        if (rc != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), RC_HANDLE_CAMERA_PERM)

            rc = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
            if (rc != PackageManager.PERMISSION_GRANTED) {
                result.error("No camera permission.", null, null)
            }
        }

        val intent: Intent
        val res: Int

        if ("scan" == call.method) {
            intent = Intent(activity, BarcodeCaptureActivity::class.java)
            res = RC_BARCODE_SCAN
        } else {
            result.notImplemented()
            return
        }

        intent.putExtra(AbstractCaptureActivity.AUTO_FOCUS, autoFocus)
        intent.putExtra(AbstractCaptureActivity.USE_FLASH, useFlash)
        intent.putExtra(AbstractCaptureActivity.FORMATS, formats)
        intent.putExtra(AbstractCaptureActivity.MULTIPLE, multiple)
        intent.putExtra(AbstractCaptureActivity.WAIT_TAP, waitTap)
        intent.putExtra(AbstractCaptureActivity.SHOW_TEXT, showText)
        intent.putExtra(AbstractCaptureActivity.CAMERA, camera)
        intent.putExtra(AbstractCaptureActivity.FPS, fps)
        activity.startActivityForResult(intent, res)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?): Boolean {
        if (requestCode == RC_BARCODE_SCAN) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (intent != null) {
                    val barcodes = intent
                            .getParcelableArrayListExtra<Barcode>(AbstractCaptureActivity.OBJECT)
                    if (!barcodes.isEmpty()) {
                        val list = ArrayList<Map<String, Any>>()
                        for (barcode in barcodes) {
                            val ret = HashMap<String, Any>()
                            ret["value"] = barcode.rawValue
                            ret["format"] = barcode.getFormatString()
                            list.add(ret)
                        }
                        result!!.success(list)
                        return true
                    }
                }
                result!!.error("No barcode captured, intent data is null", null, null)
            } else if (resultCode == CommonStatusCodes.ERROR) {
                val e = intent!!.getParcelableExtra<MobileVisionException>(AbstractCaptureActivity.ERROR)
                result!!.error(e.message, null, e)
            }
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray): Boolean {

        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            result!!.error("Got unexpected permission result: $requestCode", null, null)
            return false
        }

        if (grantResults.size != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            return true
        }

        result!!.error("Permission not granted: results len = " + grantResults.size +
                " Result code = " + if (grantResults.size > 0) grantResults[0] else "(empty)", null, null)

        return false
    }

    companion object {

        private const val RC_HANDLE_CAMERA_PERM = 2
        private const val RC_BARCODE_SCAN = 9010

        /**
         * Plugin registration.
         */
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(),
                    "com.ethras.barcode_scan")

            val plugin = BarcodeScanPlugin(registrar.activity())

            channel.setMethodCallHandler(plugin)

            registrar.addActivityResultListener(plugin)
        }
    }
}

fun  Barcode.getFormatString(): String {
    return when (this.valueFormat) {
        Barcode.ALL_FORMATS -> "ALL_FORMATS"
        Barcode.AZTEC -> "AZTEC"
        Barcode.CODABAR -> "CODABAR"
        Barcode.CODE_128 -> "CODE_128"
        Barcode.CODE_39 -> "CODE_39"
        Barcode.CODE_93 -> "CODE_93"
        Barcode.DATA_MATRIX -> "DATA_MATRIX"
        Barcode.EAN_13 -> "EAN_13"
        Barcode.EAN_8 -> "EAN_8"
        Barcode.ITF -> "ITF"
        Barcode.QR_CODE -> "QR_CODE"
        Barcode.UPC_A -> "UPC_A"
        Barcode.UPC_E -> "UPC_E"
        Barcode.PDF417 -> "PDF417"
        else -> "UNKNOWN_FORMAT"
    }
}
