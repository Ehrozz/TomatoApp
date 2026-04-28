package com.android.tomatoapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val TomatoShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(999.dp)  // Pill shape
)

// Additional shape constants
val LargeRadius = 16.dp
val SmallRadius = 10.dp
val PillRadius = 999.dp
val ExtraSmallRadius = 8.dp
val MediumRadius = 12.dp
val HeaderRadius = 18.dp
val CardRadius = 16.dp
