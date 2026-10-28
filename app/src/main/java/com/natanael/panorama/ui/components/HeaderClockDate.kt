package com.natanael.panorama.ui.components

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.provider.CalendarContract
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HeaderClockDate(
    currentTimeString: String,
    currentDateString: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val shadowStyle = Shadow(
        color = Color.Black.copy(alpha = 0.6f),
        offset = Offset(2f, 2f),
        blurRadius = 8f
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 24.dp, top = 40.dp, start = 24.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = currentTimeString.ifEmpty { "00:00" },
            style = TextStyle(
                fontSize = 54.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
                shadow = shadowStyle,
                textAlign = TextAlign.End
            ),
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                launchClockApp(context)
            }
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = currentDateString.ifEmpty { "Panorama" },
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f),
                shadow = shadowStyle,
                textAlign = TextAlign.End
            ),
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                launchCalendarApp(context)
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Panorama • by Natanael",
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.6f),
                shadow = shadowStyle,
                textAlign = TextAlign.End
            )
        )
    }
}

private fun launchClockApp(context: Context) {
    try {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val clockIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(clockIntent)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}

private fun launchCalendarApp(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_CALENDAR)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val builder = CalendarContract.CONTENT_URI.buildUpon().appendPath("time")
            val intent = Intent(Intent.ACTION_VIEW, builder.build()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}
