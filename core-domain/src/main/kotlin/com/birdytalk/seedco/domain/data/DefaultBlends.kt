package com.birdytalk.seedco.domain.data

import com.birdytalk.seedco.domain.model.Blend
import com.birdytalk.seedco.domain.model.Ingredient
import java.math.BigDecimal

/**
 * The six factory-default Birdy Talk blends used to seed the editable [Blend] store on first run
 * and when the user chooses "reset recipes to defaults".
 *
 * Percentages are declared as exact [BigDecimal] string literals so no precision is lost between
 * the recipe card and the production floor. Costs start unset (`ZERO`) for the owner to fill in.
 */
object DefaultBlends {

    private fun ingredient(name: String, percent: String) = Ingredient(name, BigDecimal(percent))

    val blends: List<Blend> = listOf(
        Blend(
            id = "cardinal_rule",
            name = "Cardinal Rule",
            tagline = "Safflower-forward, cardinal-approved.",
            ingredients = listOf(
                ingredient("Safflower", "46"),
                ingredient("Black Oil Sunflower", "30"),
                ingredient("Striped Sunflower", "10"),
                ingredient("Peanut Pickouts", "10"),
                ingredient("Dried Mealworms", "4"),
            ),
        ),
        Blend(
            id = "chickadee_chow",
            name = "Chickadee Chow",
            tagline = "High-energy fare for busy little birds.",
            ingredients = listOf(
                ingredient("Black Oil Sunflower", "50"),
                ingredient("Safflower", "30"),
                ingredient("Sunflower Chips (coarse)", "20"),
            ),
        ),
        Blend(
            id = "yard_party_mix",
            name = "Yard Party Mix",
            tagline = "A crowd-pleasing spread for the whole yard.",
            ingredients = listOf(
                ingredient("Black Oil Sunflower", "54.5"),
                ingredient("White Millet", "16.84"),
                ingredient("Striped Sunflower", "14.33"),
                ingredient("Peanut Pickouts", "14.33"),
            ),
        ),
        Blend(
            id = "blue_jay_buffet",
            name = "Blue Jay Buffet",
            tagline = "Big pieces for big, bold jays.",
            ingredients = listOf(
                ingredient("Peanut Pieces", "50"),
                ingredient("Black Oil Sunflower", "30"),
                ingredient("Striped Sunflower", "20"),
            ),
        ),
        Blend(
            id = "neat_eats",
            name = "Neat Eats",
            tagline = "No shells, no mess — pure clean eating.",
            ingredients = listOf(
                ingredient("Sunflower Chips (coarse)", "50"),
                ingredient("Peanut Pieces", "50"),
            ),
        ),
        Blend(
            id = "finch_frenzy",
            name = "Finch Frenzy",
            tagline = "Nyjer-rich blend that finches flock to.",
            ingredients = listOf(
                ingredient("Nyjer Seed", "90"),
                ingredient("Sunflower Chips (medium)", "10"),
            ),
        ),
    )
}
