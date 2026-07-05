package com.birdytalk.seedco.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.birdytalk.seedco.domain.units.UnitConverter
import com.birdytalk.seedco.domain.units.UnitSystem
import com.birdytalk.seedco.ui.theme.JetBrainsMono
import java.math.BigDecimal

/**
 * Renders a weight in the selected [unit] with a spring-physics roll-up whenever the value
 * changes. The number is drawn in the fixed-width JetBrains Mono face so its advance width never
 * jitters mid-animation; the unit label follows in a lighter tone.
 */
@Composable
fun AnimatedWeightText(
    pounds: BigDecimal,
    unit: UnitSystem,
    modifier: Modifier = Modifier,
    valueStyle: TextStyle = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    unitColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val animated by animateFloatAsState(
        targetValue = pounds.toFloat(),
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 220f,
        ),
        label = "weight",
    )

    val formatted = UnitConverter.format(
        BigDecimal(animated.toDouble()).max(BigDecimal.ZERO),
        unit,
    )

    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        Text(text = formatted.value, style = valueStyle, color = valueColor)
        if (formatted.unitLabel.isNotEmpty()) {
            Spacer(Modifier.width(4.dp))
            Text(
                text = formatted.unitLabel,
                style = MaterialTheme.typography.labelMedium,
                color = unitColor,
            )
        }
    }
}
