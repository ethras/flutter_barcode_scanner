package com.ethras.barcodescan.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.Window
import android.view.WindowManager

import com.ethras.barcodescan.ui.CameraSource
import com.ethras.barcodescan.ui.CameraSourcePreview
import com.ethras.barcodescan.ui.GraphicOverlay
import com.ethras.barcodescan.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.CommonStatusCodes

import java.io.IOException

abstract class AbstractCaptureActivity<T : GraphicOverlay.Graphic> : Activity() {

    protected var cameraSource: CameraSource? = null
    protected var preview: CameraSourcePreview? = null
    protected lateinit var graphicOverlay: GraphicOverlay<T>

    private lateinit var gestureDetector: GestureDetector

    protected var autoFocus: Boolean = false
    protected var useFlash: Boolean = false
    protected var multiple: Boolean = false
    protected var waitTap: Boolean = false
    protected var showText: Boolean = false
    protected var camera: Int = 0
    protected var fps: Float = 0.toFloat()

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE)

            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN)

            setContentView(R.layout.capture)

            preview = findViewById(R.id.preview)
            graphicOverlay = findViewById(R.id.graphic_overlay)

            autoFocus = intent.getBooleanExtra(AUTO_FOCUS, false)
            useFlash = intent.getBooleanExtra(USE_FLASH, false)
            multiple = intent.getBooleanExtra(MULTIPLE, false)
            waitTap = intent.getBooleanExtra(WAIT_TAP, false)
            showText = intent.getBooleanExtra(SHOW_TEXT, false)
            camera = intent.getIntExtra(CAMERA, CameraSource.CAMERA_FACING_BACK)
            fps = intent.getFloatExtra(FPS, 15.0f)

            val rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            if (rc == PackageManager.PERMISSION_GRANTED) {
                createCameraSource()
            } else {
                throw MobileVisionException("Camera permission is needed.")
            }

            gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    return onTap(e.rawX, e.rawY) || super.onSingleTapConfirmed(e)
                }
            })
        } catch (e: Exception) {
            onError(e)
        }

    }

    @SuppressLint("InlinedApi")
    @Throws(MobileVisionException::class)
    protected abstract fun createCameraSource()

    private fun onError(e: Exception) {
        val data = Intent()
        data.putExtra(ERROR, e)
        setResult(CommonStatusCodes.ERROR)
        finish()
    }

    override fun onResume() {
        super.onResume()
        try {
            startCameraSource()
        } catch (e: Exception) {
            onError(e)
        }

    }

    override fun onPause() {
        super.onPause()
        if (preview != null) {
            preview!!.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (preview != null) {
            preview!!.release()
        }
    }

    @SuppressLint("MissingPermission")
    @Throws(SecurityException::class, MobileVisionException::class)
    private fun startCameraSource() {

        val code = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(applicationContext)

        if (code != ConnectionResult.SUCCESS) {
            throw MobileVisionException("Google Api Availability Error: $code")
        }

        if (cameraSource != null) {
            try {
                preview!!.start(cameraSource!!, graphicOverlay)
            } catch (e: IOException) {
                cameraSource!!.release()
                cameraSource = null
                throw MobileVisionException("Unable to start camera source.", e)
            }

        }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(e) || super.onTouchEvent(e)
    }

    protected abstract fun onTap(rawX: Float, rawY: Float): Boolean

    companion object {

        val AUTO_FOCUS = "AUTO_FOCUS"
        val USE_FLASH = "USE_FLASH"
        val FORMATS = "FORMATS"
        val MULTIPLE = "MULTIPLE"
        val WAIT_TAP = "WAIT_TAP"
        val SHOW_TEXT = "SHOW_TEXT"
        val CAMERA = "CAMERA"
        val FPS = "FPS"

        val OBJECT = "Object"
        val ERROR = "Error"
    }
}
