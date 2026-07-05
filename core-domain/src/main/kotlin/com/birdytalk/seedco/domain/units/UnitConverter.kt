package com.birdytalk.seedco.domain.units

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * A weight formatted for display, split so the UI can render the numeric portion in a fixed-width
 * font (to prevent layout jitter) and the unit label separately.
 *
 * @property value The numeric portion, e.g. `"14.33"`, `"6.50"`, or the composite
 *   `"14 lb 5.3 oz"` for [UnitSystem.POUNDS_OUNCES].
 * @property unitLabel The trailing unit, e.g. `"lb"` or `"kg"`; empty when [value] already
 *   embeds its units (pounds-and-ounces).
 * @property full Convenience full string, e.g. `"14.33 lb"`.
 */
data class FormattedWeight(
    val value: String,
    val unitLabel: String,
    val full: String,
)

/**
 * Converts between the canonical unit (**pounds**, as [BigDecimal]) and the three display
 * [UnitSystem]s using exact [BigDecimal] arithmetic.
 */
object UnitConverter {

    /** Exact international avoirdupois pound → kilogram factor. */
    private val LB_TO_KG = BigDecimal("0.45359237")
    private val SIXTEEN = BigDecimal("16")
    private val MC = MathContext(20, RoundingMode.HALF_UP)

    /**
     * Interprets a numeric [value] typed by the user in the currently-selected [unit] and returns
     * the equivalent weight in canonical pounds. For [UnitSystem.POUNDS_OUNCES] the single numeric
     * field is interpreted as decimal pounds.
     */
    fun toPounds(value: BigDecimal, unit: UnitSystem): BigDecimal = when (unit) {
        UnitSystem.DECIMAL_POUNDS, UnitSystem.POUNDS_OUNCES -> value
        UnitSystem.KILOGRAMS -> value.divide(LB_TO_KG, MC)
    }

    /** Formats a canonical [pounds] weight into the given display [unit]. */
    fun format(pounds: BigDecimal, unit: UnitSystem): FormattedWeight = when (unit) {
        UnitSystem.DECIMAL_POUNDS -> {
            val v = pounds.setScale(2, RoundingMode.HALF_UP).toPlainString()
            FormattedWeight(value = v, unitLabel = "lb", full = "$v lb")
        }

        UnitSystem.KILOGRAMS -> {
            val kg = pounds.multiply(LB_TO_KG).setScale(2, RoundingMode.HALF_UP).toPlainString()
            FormattedWeight(value = kg, unitLabel = "kg", full = "$kg kg")
        }

        UnitSystem.POUNDS_OUNCES -> formatPoundsOunces(pounds)
    }

    private fun formatPoundsOunces(pounds: BigDecimal): FormattedWeight {
        // Whole pounds (floor for non-negative weights) and the fractional remainder as ounces.
        var wholePounds = pounds.setScale(0, RoundingMode.FLOOR).toBigIntegerExact()
        val remainderLb = pounds.subtract(BigDecimal(wholePounds))
        var ounces = remainderLb.multiply(SIXTEEN).setScale(1, RoundingMode.HALF_UP)

        // Rounding the ounces up can reach a full pound (e.g. 15.97 oz → 16.0); carry it.
        if (ounces.compareTo(SIXTEEN) >= 0) {
            wholePounds = wholePounds.add(java.math.BigInteger.ONE)
            ounces = BigDecimal.ZERO.setScale(1)
        }

        val value = "$wholePounds lb ${ounces.toPlainString()} oz"
        return FormattedWeight(value = value, unitLabel = "", full = value)
    }
}
