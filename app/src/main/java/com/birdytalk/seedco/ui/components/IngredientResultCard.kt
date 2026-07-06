package com.birdytalk.seedco.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.birdytalk.seedco.domain.units.UnitSystem
import com.birdytalk.seedco.ui.theme.JetBrainsMono
import com.birdytalk.seedco.ui.theme.LocalSeedCoAccents
import java.math.BigDecimal

/**
 * A high-contrast card showing one ingredient's required weight: a color key, the ingredient name
 * in serif, its recipe percentage, and the spring-animated weight in mono on the trailing edge.
 */
@Composable
fun IngredientResultCard(
    name: String,
    percent: BigDecimal,
    weightPounds: BigDecimal,
    accentColor: Color,
    unit: UnitSystem,
    modifier: Modifier = Modifier,
    costText: String? = null,
    shortMessage: String? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(accentColor),
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${trimPercent(percent)}% of blend",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (costText != null) {
                    Text(
                        text = costText,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                        color = LocalSeedCoAccents.current.gold,
                    )
                }
                if (shortMessage != null) {
                    Text(
                        text = shortMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            AnimatedWeightText(
                pounds = weightPounds,
                unit = unit,
                valueStyle = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
    }
}

// e.g. 46 -> "46", 10 -> "10", 54.5 -> "54.5", 16.84 -> "16.84"
private fun trimPercent(percent: BigDecimal): String =
    percent.stripTrailingZeros().toPlainString()
