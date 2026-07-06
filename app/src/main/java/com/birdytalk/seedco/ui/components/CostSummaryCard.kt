package com.birdytalk.seedco.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.birdytalk.seedco.domain.calc.CostSummary
import com.birdytalk.seedco.ui.format.Money
import com.birdytalk.seedco.ui.theme.JetBrainsMono
import com.birdytalk.seedco.ui.theme.LocalSeedCoAccents
import java.math.RoundingMode

/**
 * Summarizes the batch's dollar figures: total cost and blended cost-per-pound, plus revenue,
 * profit, and margin when the blend has a retail price. Renders nothing when there's no money to
 * show.
 */
@Composable
fun CostSummaryCard(
    cost: CostSummary,
    modifier: Modifier = Modifier,
) {
    val hasPricing = cost.profit != null
    if (!cost.hasCosts && !hasPricing) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Cost & margin",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (cost.hasCosts) {
                MoneyRow("Total batch cost", Money.format(cost.totalCost), emphasize = true)
                MoneyRow("Cost per lb of blend", Money.perPound(cost.costPerPound))
            }
            if (hasPricing) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                MoneyRow("Retail revenue", Money.format(cost.revenue!!))
                ProfitRow(cost)
            }
        }
    }
}

@Composable
private fun MoneyRow(label: String, value: String, emphasize: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = JetBrainsMono,
                fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Medium,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ProfitRow(cost: CostSummary) {
    val profit = cost.profit ?: return
    val positive = profit.signum() >= 0
    val accent = if (positive) LocalSeedCoAccents.current.gold else MaterialTheme.colorScheme.error
    val margin = cost.marginPercent?.setScale(1, RoundingMode.HALF_UP)?.toPlainString() ?: "—"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Profit ($margin% margin)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = Money.format(profit),
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
            ),
            color = accent,
        )
    }
}
