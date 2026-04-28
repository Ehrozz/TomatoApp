package com.android.tomatoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.tomatoapp.ui.theme.Border
import com.android.tomatoapp.ui.theme.CardRadius
import com.android.tomatoapp.ui.theme.SoftShadowElevation
import com.android.tomatoapp.ui.theme.SurfaceWhite

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = SurfaceWhite,
    contentPadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(16.dp),
    border: androidx.compose.foundation.BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(CardRadius),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = SoftShadowElevation
        ),
        border = border
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun AppCardOutlined(
    modifier: Modifier = Modifier,
    backgroundColor: Color = SurfaceWhite,
    contentPadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    AppCard(
        modifier = modifier,
        backgroundColor = backgroundColor,
        contentPadding = contentPadding,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Border),
        content = content
    )
}
