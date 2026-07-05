package com.birdytalk.seedco.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.birdytalk.seedco.ui.theme.JetBrainsMono

/**
 * A numeric entry field. The value is rendered in JetBrains Mono so typed weights stay tabular
 * and legible on the production floor. Filtering of the raw text is handled by the ViewModel.
 */
@Composable
fun NumericField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    suffix: String? = null,
    isError: Boolean = false,
    supportingText: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        suffix = suffix?.let { { Text(it, style = MaterialTheme.typography.labelLarge) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        textStyle = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMono),
        modifier = modifier.fillMaxWidth(),
    )
}
