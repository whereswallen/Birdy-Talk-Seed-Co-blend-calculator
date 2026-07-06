package com.birdytalk.seedco.domain

import com.birdytalk.seedco.domain.data.DefaultBlends
import com.birdytalk.seedco.domain.model.EditableBlend
import com.birdytalk.seedco.domain.model.EditableIngredient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class EditableBlendTest {

    @Test
    fun `editing a default blend round-trips back to an equal blend`() {
        val original = DefaultBlends.blends.first { it.id == "yard_party_mix" }
        val rebuilt = EditableBlend.from(original).toBlend()
        assertEquals(original, rebuilt)
    }

    @Test
    fun `a 50-50 draft is valid at exactly 100 percent`() {
        val draft = EditableBlend(
            id = "custom",
            name = "Custom",
            ingredients = listOf(
                EditableIngredient("A", "50", ""),
                EditableIngredient("B", "50", ""),
            ),
        )
        assertTrue(draft.isPercentValid())
        assertTrue(draft.isValid())
        assertEquals(0, BigDecimal("100").compareTo(draft.percentTotal()))
    }

    @Test
    fun `a draft that does not total 100 is invalid and cannot build a blend`() {
        val draft = EditableBlend(
            id = "custom",
            name = "Custom",
            ingredients = listOf(
                EditableIngredient("A", "50", ""),
                EditableIngredient("B", "40", ""), // totals 90
            ),
        )
        assertFalse(draft.isPercentValid())
        assertFalse(draft.isValid())
        // The domain guard rejects it even if the editor gate were bypassed.
        assertThrows(IllegalArgumentException::class.java) { draft.toBlend() }
    }

    @Test
    fun `a draft with a blank ingredient name is invalid`() {
        val draft = EditableBlend(
            id = "custom",
            name = "Custom",
            ingredients = listOf(
                EditableIngredient("", "50", ""),
                EditableIngredient("B", "50", ""),
            ),
        )
        assertFalse(draft.isValid())
    }

    @Test
    fun `costs and retail price parse through toBlend`() {
        val draft = EditableBlend(
            id = "custom",
            name = "Custom",
            retailPriceInput = "3.50",
            ingredients = listOf(
                EditableIngredient("A", "50", "1.25"),
                EditableIngredient("B", "50", ""),
            ),
        )
        val blend = draft.toBlend()
        assertEquals(0, BigDecimal("1.25").compareTo(blend.ingredients[0].costPerPound))
        assertEquals(0, BigDecimal("0").compareTo(blend.ingredients[1].costPerPound))
        assertEquals(0, BigDecimal("3.50").compareTo(blend.retailPricePerPound))
    }
}
