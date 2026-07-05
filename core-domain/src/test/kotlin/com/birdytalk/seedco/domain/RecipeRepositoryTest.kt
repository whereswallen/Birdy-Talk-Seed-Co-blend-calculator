package com.birdytalk.seedco.domain

import com.birdytalk.seedco.domain.data.RecipeRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class RecipeRepositoryTest {

    @Test
    fun `there are exactly six blends`() {
        assertEquals(6, RecipeRepository.blends.size)
    }

    @Test
    fun `every blend's percentages total exactly 100`() {
        RecipeRepository.blends.forEach { blend ->
            val sum = blend.ingredients.fold(BigDecimal.ZERO) { acc, i -> acc.add(i.percent) }
            assertEquals(
                "Blend ${blend.name} should total 100 but was $sum",
                0,
                sum.compareTo(BigDecimal("100")),
            )
        }
    }

    @Test
    fun `blend ids are unique`() {
        val ids = RecipeRepository.blends.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `lookup by id works and unknown ids return null`() {
        assertNotNull(RecipeRepository.blendById("cardinal_rule"))
        assertNull(RecipeRepository.blendById("does_not_exist"))
    }
}
