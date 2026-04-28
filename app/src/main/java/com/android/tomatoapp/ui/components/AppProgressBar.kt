package com.android.tomatoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.tomatoapp.ui.theme.GreenAccent
import com.android.tomatoapp.ui.theme.GreenLight
import com.android.tomatoapp.ui.theme.PillRadius

@Composable
fun AppProgressBar(
    progress: Float, // 0f to 1f
    modifier: Modifier = Modifier.fillMaxWidth(),
    height: Dp = 5.dp,
    backgroundColor: Color = GreenLight,
    fillColor: Color = GreenAccent,
    shape: RoundedCornerShape = RoundedCornerShape(PillRadius),
    animationDuration: Int = 300
) {
    Box(
        modifier = modifier
            .height(height)
            .background(backgroundColor, shape),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(height)
                .background(fillColor, shape)
        )
    }
}

@Composable
fun AppCircularProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    backgroundColor: Color = GreenLight,
    fillColor: Color = GreenAccent
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .background(backgroundColor, RoundedCornerShape(PillRadius))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(5.dp)
                .background(fillColor, RoundedCornerShape(PillRadius))
        )
    }
}
