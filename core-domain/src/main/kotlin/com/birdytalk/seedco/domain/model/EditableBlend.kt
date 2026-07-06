package com.birdytalk.seedco.domain.model

import java.math.BigDecimal

/** A mutable, string-backed ingredient row used while editing a blend. */
data class EditableIngredient(
    val name: String = "",
    val percentInput: String = "",
    val costInput: String = "",
)

/**
 * A mutable, string-backed draft of a [Blend] used by the editor. Because it holds raw text and
 * an arbitrary running percentage total, it can represent an in-progress (invalid) recipe — a
 * validated [Blend] is produced only via [toBlend] once [isValid] is true.
 *
 * The hard rule that a blend's percentages must total exactly 100 is enforced here ([isPercentValid])
 * as the editor's Save gate, and again by [Blend]'s own `init` check, so nothing else can persist.
 */
data class EditableBlend(
    val id: String,
    val name: String = "",
    val tagline: String = "",
    val retailPriceInput: String = "",
    val ingredients: List<EditableIngredient> = listOf(EditableIngredient()),
) {
    /** Sum of the parsed ingredient percentages (invalid/blank entries count as 0). */
    fun percentTotal(): BigDecimal =
        ingredients.fold(BigDecimal.ZERO) { acc, i ->
            acc.add(i.percentInput.toBigDecimalOrNull() ?: BigDecimal.ZERO)
        }

    /** True when the percentages total exactly 100. */
    fun isPercentValid(): Boolean = Blend.percentagesTotalOneHundred(parsedIngredients())

    /** True when every ingredient has a name and a strictly-positive, parseable percentage. */
    fun ingredientsWellFormed(): Boolean =
        ingredients.isNotEmpty() && ingredients.all { row ->
            row.name.isNotBlank() && (row.percentInput.toBigDecimalOrNull()?.signum() ?: 0) > 0
        }

    /** Full validity: named blend, well-formed ingredients, and an exact 100% total. */
    fun isValid(): Boolean = name.isNotBlank() && ingredientsWellFormed() && isPercentValid()

    /**
     * Builds a validated [Blend]. Callers should gate on [isValid]; [Blend]'s `init` still enforces
     * the 100% rule as a backstop and will throw otherwise.
     */
    fun toBlend(): Blend = Blend(
        id = id,
        name = name.trim(),
        tagline = tagline.trim(),
        ingredients = ingredients.map { row ->
            Ingredient(
                name = row.name.trim(),
                percent = row.percentInput.toBigDecimal(),
                costPerPound = row.costInput.toBigDecimalOrNull() ?: BigDecimal.ZERO,
            )
        },
        retailPricePerPound = retailPriceInput.toBigDecimalOrNull()?.takeIf { it.signum() > 0 },
    )

    private fun parsedIngredients(): List<Ingredient> =
        ingredients.mapNotNull { row ->
            val percent = row.percentInput.toBigDecimalOrNull() ?: return@mapNotNull null
            Ingredient(row.name, percent)
        }

    companion object {
        /** Wraps an existing [blend] for editing. */
        fun from(blend: Blend): EditableBlend = EditableBlend(
            id = blend.id,
            name = blend.name,
            tagline = blend.tagline,
            retailPriceInput = blend.retailPricePerPound?.stripTrailingZeros()?.toPlainString().orEmpty(),
            ingredients = blend.ingredients.map { ing ->
                EditableIngredient(
                    name = ing.name,
                    percentInput = ing.percent.stripTrailingZeros().toPlainString(),
                    costInput = ing.costPerPound.takeIf { it.signum() > 0 }
                        ?.stripTrailingZeros()?.toPlainString().orEmpty(),
                )
            },
        )

        /** A fresh, empty draft for a new blend with the given [id]. */
        fun blank(id: String): EditableBlend = EditableBlend(id = id)
    }
}
