package com.example.artsphere.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * A reusable component for displaying OutlinedTextField ensuring consistent styling
 *
 * @param value value being manipulated by the user
 * @param onValueChange Callback triggered when the value changes.
 * @param label label of the field
 * @param icon icon to the left of the field, default null
 * @param keyboardType specified keyboard type, default Text
 * @param isPassword checks if value being inputted is a password, default false
 * @param passwordVisible checks if password is visible, default false
 * @param onPasswordVisibilityChange Callback triggered when password visibility changes.
 * @param isSubmitted checks if the field has been submitted, default false
 * @param lines number of lines the field can take, default 1
 * @param modifier Modifier for the field, default fillMaxWidth()
 * @param supportingText text to be displayed below the field, default null
 *
 */
@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityChange: () -> Unit = {},
    isSubmitted: Boolean = false,
    lines: Int = 1,
    modifier: Modifier = Modifier.fillMaxWidth(),
    supportingText: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        // if icon is not null, display it
        leadingIcon = if (icon != null) {
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            null
        },
        // if password, display visibility icon
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = onPasswordVisibilityChange) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Color.Gray
                    )
                }
            }
        },
        // if password, hide text
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = modifier,
        maxLines = lines,
        singleLine = (lines <= 1),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            // if submitted and empty, show error
            unfocusedBorderColor = if (isSubmitted && value.isBlank()) Color.Red else Color.Gray,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSecondary,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.onSecondary,
            unfocusedTextColor = MaterialTheme.colorScheme.onSecondary
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        // if submitted and empty, show error
        isError = isSubmitted && value.isBlank(),
        // if supportingText is not null, display it
        supportingText = if (supportingText != null) {
            {
                Text(
                    text = supportingText,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            null
        }
    )
}
