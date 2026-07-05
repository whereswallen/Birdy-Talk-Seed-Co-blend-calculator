package com.birdytalk.seedco.domain

import com.birdytalk.seedco.domain.units.UnitConverter
import com.birdytalk.seedco.domain.units.UnitSystem
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class UnitConverterTest {

    @Test
    fun `decimal pounds formats to two places`() {
        val f = UnitConverter.format(BigDecimal("14.33"), UnitSystem.DECIMAL_POUNDS)
        assertEquals("14.33", f.value)
        assertEquals("lb", f.unitLabel)
        assertEquals("14.33 lb", f.full)
    }

    @Test
    fun `pounds convert to kilograms using the exact factor`() {
        val f = UnitConverter.format(BigDecimal("14.33"), UnitSystem.KILOGRAMS)
        // 14.33 * 0.45359237 = 6.4999... -> 6.50
        assertEquals("6.50", f.value)
        assertEquals("6.50 kg", f.full)
    }

    @Test
    fun `pounds and ounces splits the fractional remainder`() {
        val f = UnitConverter.format(BigDecimal("14.33"), UnitSystem.POUNDS_OUNCES)
        // 0.33 lb * 16 = 5.28 oz -> 5.3
        assertEquals("14 lb 5.3 oz", f.value)
        assertEquals("", f.unitLabel)
    }

    @Test
    fun `ounces rounding that reaches a full pound carries over`() {
        // 0.9999 lb -> 15.9984 oz -> rounds to 16.0 -> carry to 1 lb 0.0 oz
        val f = UnitConverter.format(BigDecimal("0.9999"), UnitSystem.POUNDS_OUNCES)
        assertEquals("1 lb 0.0 oz", f.value)
    }

    @Test
    fun `whole pounds show zero ounces`() {
        val f = UnitConverter.format(BigDecimal("7"), UnitSystem.POUNDS_OUNCES)
        assertEquals("7 lb 0.0 oz", f.value)
    }

    @Test
    fun `kilogram input converts back to pounds`() {
        val pounds = UnitConverter.toPounds(BigDecimal("6.50"), UnitSystem.KILOGRAMS)
        // 6.50 / 0.45359237 ~= 14.3300 lb
        assertEquals(0, pounds.setScale(2, java.math.RoundingMode.HALF_UP).compareTo(BigDecimal("14.33")))
    }

    @Test
    fun `pound-family input passes through unchanged`() {
        assertEquals(
            0,
            UnitConverter.toPounds(BigDecimal("14.33"), UnitSystem.DECIMAL_POUNDS)
                .compareTo(BigDecimal("14.33")),
        )
        assertEquals(
            0,
            UnitConverter.toPounds(BigDecimal("14.33"), UnitSystem.POUNDS_OUNCES)
                .compareTo(BigDecimal("14.33")),
        )
    }
}
