package com.android.tomatoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.tomatoapp.ui.theme.Border
import com.android.tomatoapp.ui.theme.DmSansFamily
import com.android.tomatoapp.ui.theme.RedPrimary
import com.android.tomatoapp.ui.theme.SurfaceWhite
import com.android.tomatoapp.ui.theme.TextMuted

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
    val badge: Int? = null
)

@Composable
fun AppBottomNavBar(
    items: List<BottomNavItem>,
    selectedRoute: String?,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(SurfaceWhite)
            .border(
                width = 0.5.dp,
                color = Border
            )
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val isSelected = item.route == selectedRoute
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.clickableNavItem { onItemSelected(item.route) }
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(20.dp),
                        tint = if (isSelected) RedPrimary else TextMuted
                    )
                    Text(
                        text = item.label,
                        style = TextStyle(
                            fontFamily = DmSansFamily,
                            fontSize = 10.sp,
                            color = if (isSelected) RedPrimary else TextMuted
                        ),
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun clickableNavItem(onClick: () -> Unit): Modifier {
    return Modifier
}
