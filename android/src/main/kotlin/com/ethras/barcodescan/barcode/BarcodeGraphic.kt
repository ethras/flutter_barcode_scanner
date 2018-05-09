/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ethras.barcodescan.barcode

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

import com.ethras.barcodescan.ui.GraphicOverlay
import com.google.android.gms.vision.barcode.Barcode

/**
 * Graphic instance for rendering barcode position, size, and ID within an associated graphic
 * overlay view.
 */
class BarcodeGraphic internal constructor(overlay: GraphicOverlay<*>, showText: Boolean) : GraphicOverlay.Graphic(overlay) {
    private val rectPaint: Paint
    private var textPaint: Paint? = null
    @Volatile
    var barcode: Barcode? = null
        private set

    init {

        currentColorIndex = (currentColorIndex + 1) % COLOR_CHOICES.size
        val selectedColor = COLOR_CHOICES[currentColorIndex]

        rectPaint = Paint()
        rectPaint.color = selectedColor
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = 4.0f

        if (showText) {
            textPaint = Paint()
            textPaint!!.color = selectedColor
            textPaint!!.textSize = 36.0f
        }
    }

    override val boundingBox: RectF?
        get() {
        val barcode = this.barcode ?: return null
        var rect = RectF(barcode.boundingBox)
        rect = translateRect(rect)
        return rect
    }

    /**
     * Updates the barcode instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    fun updateItem(barcode: Barcode) {
        this.barcode = barcode
        postInvalidate()
    }

    /**
     * Draws the barcode annotations for position, size, and raw value on the supplied canvas.
     */
    override fun draw(canvas: Canvas) {
        val barcode = this.barcode ?: return

        // Draws the bounding box around the barcode.
        var rect = RectF(barcode.boundingBox)
        rect = translateRect(rect)
        canvas.drawRect(rect, rectPaint)

        if (textPaint != null) {
            // Draws a label at the bottom of the barcode indicate the barcode value that was detected.
            canvas.drawText(barcode.rawValue, rect.left, rect.bottom, textPaint!!)
        }
    }

    companion object {

        private val COLOR_CHOICES = intArrayOf(Color.GREEN, Color.BLUE, Color.RED, Color.YELLOW)
        private var currentColorIndex: Int = 0

        init {
            currentColorIndex = 0
        }
    }
}
