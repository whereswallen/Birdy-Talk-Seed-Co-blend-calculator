package com.birdytalk.seedco.domain

import com.birdytalk.seedco.domain.data.DefaultBlends
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class DefaultBlendsTest {

    @Test
    fun `there are exactly six default blends`() {
        assertEquals(6, DefaultBlends.blends.size)
    }

    @Test
    fun `every default blend's percentages total exactly 100`() {
        DefaultBlends.blends.forEach { blend ->
            val sum = blend.ingredients.fold(BigDecimal.ZERO) { acc, i -> acc.add(i.percent) }
            assertEquals(
                "Blend ${blend.name} should total 100 but was $sum",
                0,
                sum.compareTo(BigDecimal("100")),
            )
        }
    }

    @Test
    fun `default blend ids are unique`() {
        val ids = DefaultBlends.blends.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `default ingredient costs start at zero`() {
        DefaultBlends.blends.flatMap { it.ingredients }.forEach { ingredient ->
            assertEquals(0, ingredient.costPerPound.signum())
        }
    }
}
