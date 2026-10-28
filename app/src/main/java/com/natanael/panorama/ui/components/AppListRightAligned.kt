package com.natanael.panorama.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.natanael.panorama.model.AppItem

@Composable
fun AppListRightAligned(
    apps: List<AppItem>,
    selectedLetter: Char?,
    onAppClick: (AppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val shadowStyle = Shadow(
        color = Color.Black.copy(alpha = 0.6f),
        offset = Offset(2f, 2f),
        blurRadius = 6f
    )

    if (apps.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(end = 50.dp, start = 24.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            val emptyMessage = if (selectedLetter != null && selectedLetter != '☆') {
                "No apps starting with '$selectedLetter'"
            } else {
                "No favorite apps yet\n(Long press background for Settings)"
            }
            Text(
                text = emptyMessage,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.6f),
                    shadow = shadowStyle,
                    textAlign = TextAlign.End
                )
            )
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .padding(end = 48.dp, start = 16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(
                items = apps,
                key = { app -> app.uniqueKey }
            ) { app ->
                AppItemRow(
                    app = app,
                    onAppClick = onAppClick,
                    shadowStyle = shadowStyle
                )
            }
        }
    }
}

@Composable
private fun AppItemRow(
    app: AppItem,
    onAppClick: (AppItem) -> Unit,
    shadowStyle: Shadow
) {
    Row(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onAppClick(app)
            }
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        val labelText = if (app.isClone) "${app.label} (Clone)" else app.label

        Text(
            text = labelText,
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = if (app.isClone) Color(0xFF80D8FF) else Color.White,
                shadow = shadowStyle,
                textAlign = TextAlign.End
            )
        )

        Spacer(modifier = Modifier.width(12.dp))

        if (app.iconBitmap != null) {
            Image(
                bitmap = app.iconBitmap,
                contentDescription = app.label,
                modifier = Modifier.size(28.dp)
            )
        } else if (app.isFavorite) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = "Favorite",
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
