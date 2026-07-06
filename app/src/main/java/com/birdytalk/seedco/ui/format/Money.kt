package com.birdytalk.seedco.ui.format

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Single place that formats dollar amounts, so the currency symbol can be changed app-wide in
 * one edit.
 */
object Money {
    const val SYMBOL = "$"

    /** e.g. 1.5 → "$1.50". */
    fun format(amount: BigDecimal): String =
        SYMBOL + amount.setScale(2, RoundingMode.HALF_UP).toPlainString()

    /** e.g. 1.5 → "$1.50 / lb". */
    fun perPound(amount: BigDecimal): String = "${format(amount)} / lb"
}
