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
package com.ethras.barcodescan.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

import com.google.android.gms.vision.CameraSource

import java.util.HashSet
import java.util.Vector

/**
 * A view which renders a series of custom graphics to be overlayed on top of an associated preview
 * (i.e., the camera preview).  The creator can add graphics objects, update the objects, and remove
 * them, triggering the appropriate drawing and invalidation within the view.
 *
 *
 *
 *
 * Supports scaling and mirroring of the graphics relative the camera's preview properties.  The
 * idea is that detection items are expressed in terms of a preview size, but need to be scaled up
 * to the full view size, and also mirrored in the case of the front-facing camera.
 *
 *
 *
 *
 * Associated [Graphic] items should use the following methods to convert to view coordinates
 * for the graphics that are drawn:
 *
 *  1. [Graphic.scaleX] and [Graphic.scaleY] adjust the size of the
 * supplied value from the preview scale to the view scale.
 *  1. [Graphic.translateX] and [Graphic.translateY] adjust the coordinate
 * from the preview's coordinate system to the view coordinate system.
 *
 */
class GraphicOverlay<T : GraphicOverlay.Graphic>(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val lock = Any()
    private var previewWidth: Int = 0
    private var widthScaleFactor = 1.0f
    private var previewHeight: Int = 0
    private var heightScaleFactor = 1.0f
    private var facing = CameraSource.CAMERA_FACING_BACK
    internal val graphics = HashSet<T>()

    /**
     * Base class for a custom graphics object to be rendered within the graphic overlay.  Subclass
     * this and implement the [Graphic.draw] method to define the
     * graphics element.  Add instances to the overlay using [GraphicOverlay.add].
     */
    abstract class Graphic(private val overlay: GraphicOverlay<*>) {
        var id: Int = 0

        abstract val boundingBox : RectF?

        abstract fun draw(canvas: Canvas)

        fun contains(x: Float, y: Float): Boolean {
            val rect = boundingBox
            return rect != null && (rect.left < x && rect.right > x
                    && rect.top < y && rect.bottom > y)
        }

        fun scaleX(horizontal: Float): Float {
            return horizontal * overlay.widthScaleFactor
        }

        fun scaleY(vertical: Float): Float {
            return vertical * overlay.heightScaleFactor
        }

        fun translateX(x: Float): Float {
            return if (overlay.facing == CameraSource.CAMERA_FACING_FRONT) {
                overlay.width - scaleX(x)
            } else {
                scaleX(x)
            }
        }

        fun translateY(y: Float): Float {
            return scaleY(y)
        }

        fun translateRect(inputRect: RectF): RectF {
            val returnRect = RectF()

            returnRect.left = translateX(inputRect.left)
            returnRect.top = translateY(inputRect.top)
            returnRect.right = translateX(inputRect.right)
            returnRect.bottom = translateY(inputRect.bottom)

            return returnRect
        }

        fun postInvalidate() {
            overlay.postInvalidate()
        }
    }

    /**
     * Removes all graphics from the overlay.
     */
    fun clear() {
        synchronized(lock) {
            graphics.clear()
        }
        postInvalidate()
    }

    /**
     * Adds a graphic to the overlay.
     */
    fun add(graphic: T) {
        synchronized(lock) {
            graphics.add(graphic)
        }
        postInvalidate()
    }

    /**
     * Removes a graphic from the overlay.
     */
    fun remove(graphic: T) {
        synchronized(lock) {
            graphics.remove(graphic)
        }
        postInvalidate()
    }

    fun getBest(rawX: Float, rawY: Float): T? {
        synchronized(lock) {
            var best: T? = null

            val location = IntArray(2)
            getLocationOnScreen(location)
            val x = (rawX - location[0]) / widthScaleFactor
            val y = (rawY - location[1]) / heightScaleFactor

            var bestDistance = java.lang.Float.MAX_VALUE

            for (graphic in graphics) {
                if (graphic.contains(x, y)) {
                    best = graphic
                    break
                }
                val rect = graphic.boundingBox
                if (rect != null) {
                    val dx = x - graphic.boundingBox!!.centerX()
                    val dy = y - graphic.boundingBox!!.centerY()
                    val distance = dx * dx + dy * dy
                    if (distance < bestDistance) {
                        best = graphic
                        bestDistance = distance
                    }
                }
            }

            return best
        }
    }

    /**
     * Returns a copy (as a list) of the set of all active graphics.
     *
     * @return list of all active graphics.
     */
    fun getGraphics(): List<T> {
        synchronized(lock) {
            return Vector(graphics)
        }
    }

    /**
     * Sets the camera attributes for size and facing direction, which informs how to transform
     * image coordinates later.
     */
    fun setCameraInfo(previewWidth: Int, previewHeight: Int, facing: Int) {
        synchronized(lock) {
            this.previewWidth = previewWidth
            this.previewHeight = previewHeight
            this.facing = facing
        }
        postInvalidate()
    }

    /**
     * Draws the overlay with its associated graphic objects.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        synchronized(lock) {
            if (previewWidth != 0 && previewHeight != 0) {
                widthScaleFactor = canvas.width.toFloat() / previewWidth.toFloat()
                heightScaleFactor = canvas.height.toFloat() / previewHeight.toFloat()
            }

            for (graphic in graphics) {
                graphic.draw(canvas)
            }
        }
    }
}