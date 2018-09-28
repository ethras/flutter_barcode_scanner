package com.ethras.barcodescan.barcode

import com.google.android.gms.vision.barcode.Barcode


enum class BarcodeFormats(private val intValue: Int) {
    ALL_FORMATS(Barcode.ALL_FORMATS),
    CODE_128(Barcode.CODE_128),
    CODE_39(Barcode.CODE_39),
    CODE_93(Barcode.CODE_93),
    CODABAR(Barcode.CODABAR),
    DATA_MATRIX(Barcode.DATA_MATRIX),
    EAN_13(Barcode.EAN_13),
    EAN_8(Barcode.EAN_8),
    ITF(Barcode.ITF),
    QR_CODE(Barcode.QR_CODE),
    UPC_A(Barcode.UPC_A),
    UPC_E(Barcode.UPC_E),
    PDF417(Barcode.PDF417),
    AZTEC(Barcode.AZTEC);

    companion object {
        private var formatsMap: MutableMap<String, Int> = hashMapOf()

        init {
            val values = BarcodeFormats.values()
            values.forEach { value ->
                formatsMap[value.toString()] = value.intValue
            }
        }

        /**
         * Return the integer value resuling from OR-ing all of the values
         * of the supplied strings.
         *
         *
         * Note that if ALL_FORMATS is defined as well as other values, ALL_FORMATS
         * will be ignored (following how it would work with just OR-ing the ints).
         *
         * @param strings - list of strings representing the various formats
         * @return integer value corresponding to OR of all the values.
         */
        fun intFromStringList(strings: List<String>?): Int {
            if (strings == null) return BarcodeFormats.ALL_FORMATS.intValue
            var value = 0
            for (string in strings) {
                val asInt = BarcodeFormats.formatsMap[string]
                if (asInt != null) {
                    value = value or asInt
                }
            }
            return value
        }
    }


}