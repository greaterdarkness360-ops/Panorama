package com.natanael.panorama.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt

val SCRUBBER_ALPHABET: List<Char> = listOf('☆') + ('A'..'Z').toList() + listOf('#')

@Composable
fun AlphabetWaveScrubber(
    selectedLetter: Char?,
    onLetterSelected: (Char?) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    var totalHeightPx by remember { mutableFloatStateOf(0f) }
    var touchYPx by remember { mutableFloatStateOf(-1f) }
    var isScrubbingInternal by remember { mutableStateOf(false) }

    fun updateSelectedLetter(yPx: Float) {
        if (totalHeightPx <= 0f) return
        val clampedY = yPx.coerceIn(0f, totalHeightPx)
        val itemCount = SCRUBBER_ALPHABET.size
        val index = ((clampedY / totalHeightPx) * itemCount).toInt().coerceIn(0, itemCount - 1)
        val newLetter = SCRUBBER_ALPHABET[index]
        if (newLetter != selectedLetter) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onLetterSelected(newLetter)
        }
    }

    Box(
        modifier = modifier
            .width(36.dp)
            .fillMaxHeight()
            .onGloballyPositioned { coordinates ->
                totalHeightPx = coordinates.size.height.toFloat()
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        isScrubbingInternal = true
                        touchYPx = offset.y
                        updateSelectedLetter(offset.y)
                        tryAwaitRelease()
                        isScrubbingInternal = false
                        touchYPx = -1f
                        onLetterSelected(null)
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isScrubbingInternal = true
                        touchYPx = offset.y
                        updateSelectedLetter(offset.y)
                    },
                    onDrag = { change, _ ->
                        touchYPx = change.position.y
                        updateSelectedLetter(change.position.y)
                    },
                    onDragEnd = {
                        isScrubbingInternal = false
                        touchYPx = -1f
                        onLetterSelected(null)
                    },
                    onDragCancel = {
                        isScrubbingInternal = false
                        touchYPx = -1f
                        onLetterSelected(null)
                    }
                )
            },
        contentAlignment = Alignment.CenterEnd
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.End
        ) {
            val itemCount = SCRUBBER_ALPHABET.size

            SCRUBBER_ALPHABET.forEachIndexed { index, char ->
                val itemCenterYPx = if (totalHeightPx > 0f) {
                    (index + 0.5f) * (totalHeightPx / itemCount)
                } else 0f

                val targetXOffsetDp = if (isScrubbingInternal && touchYPx >= 0f && totalHeightPx > 0f) {
                    val distancePx = abs(itemCenterYPx - touchYPx)
                    val sigmaPx = with(density) { 50.dp.toPx() }
                    val maxOffsetDp = 48f
                    val waveFactor = exp(-(distancePx * distancePx) / (2 * sigmaPx * sigmaPx))
                    -maxOffsetDp * waveFactor
                } else {
                    0f
                }

                val animatedXOffsetDp by animateFloatAsState(
                    targetValue = targetXOffsetDp,
                    animationSpec = spring(stiffness = 500f, dampingRatio = 0.8f),
                    label = "waveOffset"
                )

                val isSelected = (char == selectedLetter)

                val textStyle = TextStyle(
                    fontSize = if (isSelected) 16.sp else 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(1f, 1f),
                        blurRadius = 4f
                    )
                )

                Text(
                    text = char.toString(),
                    style = textStyle,
                    modifier = Modifier
                        .offset { IntOffset(x = animatedXOffsetDp.dp.roundToPx(), y = 0) }
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}
