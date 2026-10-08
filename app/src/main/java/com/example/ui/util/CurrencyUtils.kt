package com.example.ui.util

import java.text.NumberFormat
import java.util.Locale

/**
 * High-performance, thread-safe currency formatting utility.
 * Reusing a single NumberFormat instance avoids heavy GC pressure and recomposition lag.
 */
object CurrencyUtils {
    private val inrFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")).apply {
        maximumFractionDigits = 0
    }

    fun formatRent(amount: Number): String {
        return synchronized(inrFormat) {
            inrFormat.format(amount)
        }
    }
}
