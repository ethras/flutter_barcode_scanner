package com.ethras.barcodescan.util

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
internal class MobileVisionException(override val message: String, override val cause: Throwable?) : Exception(message, cause), Parcelable {
    constructor(message: String): this(message, null)
}