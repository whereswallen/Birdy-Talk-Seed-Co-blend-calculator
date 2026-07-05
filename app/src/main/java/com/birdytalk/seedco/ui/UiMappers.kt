package com.birdytalk.seedco.ui

import com.birdytalk.seedco.domain.model.Blend
import com.birdytalk.seedco.ui.components.CompositionSlice
import com.birdytalk.seedco.ui.components.DropdownOption
import com.birdytalk.seedco.ui.components.ingredientColor

/** Blend list → dropdown options. */
fun List<Blend>.toBlendOptions(): List<DropdownOption> =
    map { DropdownOption(id = it.id, label = it.name) }

/** A blend's ingredients → dropdown options (id and label are the ingredient name). */
fun Blend.toIngredientOptions(): List<DropdownOption> =
    ingredients.map { DropdownOption(id = it.name, label = it.name) }

/** A blend's ingredients → colored composition slices, in recipe order. */
fun Blend.toSlices(): List<CompositionSlice> =
    ingredients.mapIndexed { index, ingredient ->
        CompositionSlice(
            label = ingredient.name,
            percent = ingredient.percent.toFloat(),
            color = ingredientColor(index),
        )
    }
