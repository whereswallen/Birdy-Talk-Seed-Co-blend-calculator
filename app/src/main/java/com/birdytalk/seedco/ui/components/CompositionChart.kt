package com.birdytalk.seedco.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/** One wedge/segment of a composition chart. */
data class CompositionSlice(
    val label: String,
    val percent: Float,
    val color: Color,
)

/**
 * A clean, custom-drawn donut chart. Wedges sweep in with a short tween whenever the underlying
 * [slices] change (e.g. a new blend is selected).
 */
@Composable
fun CompositionDonut(
    slices: List<CompositionSlice>,
    modifier: Modifier = Modifier,
    animationKey: Any = slices,
) {
    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 650),
        label = "donut-$animationKey",
    )
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(modifier = modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(6.dp)) {
            val stroke = size.minDimension * 0.16f
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(inset, inset)

            // Base track.
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Butt),
            )

            var startAngle = -90f
            val total = slices.sumOf { it.percent.toDouble() }.toFloat().coerceAtLeast(0.0001f)
            slices.forEach { slice ->
                val sweep = (slice.percent / total) * 360f * progress
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Butt),
                )
                startAngle += (slice.percent / total) * 360f
            }
        }
        Text(
            text = "${slices.size}\ningredients",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

/**
 * A stacked horizontal ratio bar — an at-a-glance alternative to the donut, good for wide layouts.
 */
@Composable
fun RatioBar(
    slices: List<CompositionSlice>,
    modifier: Modifier = Modifier,
) {
    val total = slices.sumOf { it.percent.toDouble() }.toFloat().coerceAtLeast(0.0001f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(18.dp)
            .clip(RoundedCornerShape(9.dp)),
    ) {
        slices.forEach { slice ->
            val weight by animateFloatAsState(
                targetValue = slice.percent / total,
                animationSpec = tween(500),
                label = "ratio-${slice.label}",
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(weight.coerceAtLeast(0.0001f))
                    .background(slice.color)
                    .height(18.dp),
            )
        }
    }
}

/** A wrapped legend of colored dots + ingredient names for the composition charts. */
@Composable
fun CompositionLegend(
    slices: List<CompositionSlice>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        slices.forEach { slice ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(slice.color),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = slice.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${trimPercent(slice.percent)}%",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = com.birdytalk.seedco.ui.theme.JetBrainsMono,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun trimPercent(percent: Float): String {
    val rounded = Math.round(percent * 100f) / 100f
    return if (rounded % 1f == 0f) rounded.toInt().toString() else rounded.toString()
}
