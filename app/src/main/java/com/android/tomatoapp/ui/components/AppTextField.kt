package com.android.tomatoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.tomatoapp.ui.theme.Border
import com.android.tomatoapp.ui.theme.CreamBackground
import com.android.tomatoapp.ui.theme.DmSansFamily
import com.android.tomatoapp.ui.theme.PillRadius
import com.android.tomatoapp.ui.theme.TextMuted
import com.android.tomatoapp.ui.theme.TextPrimary

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    label: String = "",
    placeholder: String = "",
    isPassword: Boolean = false,
    enabled: Boolean = true,
    backgroundColor: Color = CreamBackground,
    borderColor: Color = Border,
    textColor: Color = TextPrimary
) {
    val visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        visualTransformation = visualTransformation,
        textStyle = TextStyle(
            fontFamily = DmSansFamily,
            fontSize = 14.sp,
            color = textColor
        ),
        placeholder = {
            Text(
                text = placeholder,
                style = TextStyle(
                    fontFamily = DmSansFamily,
                    fontSize = 14.sp,
                    color = TextMuted
                )
            )
        },
        label = if (label.isNotEmpty()) {
            {
                Text(
                    text = label,
                    style = TextStyle(
                        fontFamily = DmSansFamily,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                )
            }
        } else null,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = backgroundColor,
            unfocusedContainerColor = backgroundColor,
            disabledContainerColor = backgroundColor,
            focusedIndicatorColor = borderColor,
            unfocusedIndicatorColor = borderColor,
            cursorColor = TextPrimary
        ),
        shape = RoundedCornerShape(PillRadius),
        singleLine = true
    )
}

@Composable
fun AppTextFieldSmall(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    backgroundColor: Color = CreamBackground,
    borderColor: Color = Border,
    textColor: Color = TextPrimary
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(10.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(10.dp)),
        enabled = true,
        textStyle = TextStyle(
            fontFamily = DmSansFamily,
            fontSize = 13.sp,
            color = textColor
        ),
        placeholder = {
            Text(
                text = placeholder,
                style = TextStyle(
                    fontFamily = DmSansFamily,
                    fontSize = 13.sp,
                    color = TextMuted
                )
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = TextPrimary
        ),
        shape = RoundedCornerShape(10.dp),
        singleLine = true
    )
}
