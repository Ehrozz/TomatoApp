package com.android.tomatoapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.tomatoapp.ui.theme.Border
import com.android.tomatoapp.ui.theme.DmSansFamily
import com.android.tomatoapp.ui.theme.PillRadius
import com.android.tomatoapp.ui.theme.RedPrimary
import com.android.tomatoapp.ui.theme.SurfaceWhite
import com.android.tomatoapp.ui.theme.TextMuted
import com.android.tomatoapp.ui.theme.TextPrimary

@Composable
fun AppButtonPrimary(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = RedPrimary,
    textColor: Color = Color.White,
    contentPaddingValues: PaddingValues = PaddingValues(vertical = 15.dp, horizontal = 16.dp)
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(PillRadius),
        contentPadding = contentPaddingValues
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = DmSansFamily,
                fontSize = 14.sp,
                color = textColor
            )
        )
    }
}

@Composable
fun AppButtonOutlined(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    borderColor: Color = Border,
    textColor: Color = TextPrimary,
    backgroundColor: Color = SurfaceWhite,
    contentPaddingValues: PaddingValues = PaddingValues(vertical = 13.dp, horizontal = 16.dp)
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = backgroundColor,
            contentColor = textColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.5.dp, borderColor),
        shape = RoundedCornerShape(PillRadius),
        contentPadding = contentPaddingValues
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = DmSansFamily,
                fontSize = 13.sp,
                color = textColor
            )
        )
    }
}

@Composable
fun AppButtonSmall(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = RedPrimary,
    textColor: Color = Color.White
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(PillRadius),
        contentPadding = PaddingValues(vertical = 7.dp, horizontal = 14.dp)
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = DmSansFamily,
                fontSize = 11.sp,
                color = textColor
            )
        )
    }
}
