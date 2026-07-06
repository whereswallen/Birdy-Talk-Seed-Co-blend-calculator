package com.birdytalk.seedco.ui.format

import com.birdytalk.seedco.domain.calc.CostSummary
import com.birdytalk.seedco.domain.units.UnitConverter
import com.birdytalk.seedco.domain.units.UnitSystem
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Builds the plain-text production batch sheet shared via the Android share sheet. Reads weights
 * and costs straight off a [CostSummary] so the sheet always matches the on-screen numbers.
 */
object BatchSheet {

    private val dateFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US)

    fun build(
        title: String,
        subtitle: String,
        unit: UnitSystem,
        cost: CostSummary,
    ): String = buildString {
        appendLine("BIRDY TALK BLEND MANAGER")
        appendLine(title)
        if (subtitle.isNotBlank()) appendLine(subtitle)
        appendLine(dateFormat.format(Date()))
        appendLine("Units: ${unit.label}")
        appendLine("--------------------------------")
        cost.lines.forEach { line ->
            val weight = UnitConverter.format(line.weightPounds, unit).full
            val costPart = if (cost.hasCosts) "   ${Money.format(line.cost)}" else ""
            appendLine("• ${line.ingredient.name}: $weight$costPart")
        }
        appendLine("--------------------------------")
        appendLine("Total batch: ${UnitConverter.format(cost.totalPounds, unit).full}")
        if (cost.hasCosts) {
            appendLine("Total cost: ${Money.format(cost.totalCost)}")
            appendLine("Cost per lb: ${Money.format(cost.costPerPound)}")
        }
        if (cost.profit != null && cost.revenue != null) {
            appendLine("Revenue: ${Money.format(cost.revenue!!)}")
            val margin = cost.marginPercent?.setScale(1, RoundingMode.HALF_UP)?.toPlainString() ?: "—"
            appendLine("Profit: ${Money.format(cost.profit!!)}  ($margin% margin)")
        }
    }
}
