package com.birdytalk.seedco.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.birdytalk.seedco.ui.theme.LocalSeedCoAccents

/**
 * A soft-amber safety container shown when a computed batch exceeds the configured bin capacity.
 * The caller wraps this in `AnimatedVisibility` so it eases in and out.
 */
@Composable
fun CapacityWarningBanner(
    message: String,
    detail: String,
    modifier: Modifier = Modifier,
) {
    val accents = LocalSeedCoAccents.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(accents.warningContainer)
            .border(1.dp, accents.warningBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.WarningAmber,
            contentDescription = null,
            tint = accents.warningBorder,
            modifier = Modifier.size(28.dp),
        )
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                text = message,
                style = MaterialTheme.typography.titleSmall,
                color = accents.onWarningContainer,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = accents.onWarningContainer,
            )
        }
    }
}
