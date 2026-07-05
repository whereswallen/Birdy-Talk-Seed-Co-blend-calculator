package com.birdytalk.seedco.ui.components

import androidx.compose.ui.graphics.Color

/**
 * A curated earthy palette used to color composition-chart segments. Colors are assigned by the
 * ingredient's position within its blend, so the same ingredient keeps a stable hue across the
 * donut, the ratio bar, and its result card.
 */
private val IngredientPalette = listOf(
    Color(0xFFC9A34E), // muted gold
    Color(0xFF7C9A6B), // sage
    Color(0xFFB5651D), // terracotta
    Color(0xFF8C7B9C), // dusty plum
    Color(0xFF4E7A6B), // deep teal-green
    Color(0xFFD98C5F), // warm clay
    Color(0xFF6B8CAE), // muted slate blue
)

fun ingredientColor(index: Int): Color = IngredientPalette[index % IngredientPalette.size]
