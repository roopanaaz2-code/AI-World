package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MetallicGold

@Composable
fun AnimatedWaveform(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    height: Dp = 64.dp,
    isSpeaking: Boolean = false,
    isListening: Boolean = false
) {
    val barBrush = when {
        isSpeaking -> Brush.verticalGradient(
            colors = listOf(
                MetallicGold,
                Color(0xFFFFB300),
                ElectricBlue
            )
        )
        isListening -> Brush.verticalGradient(
            colors = listOf(
                ElectricBlue,
                Color(0xFF0066FF),
                Color(0xFF38BDF8)
            )
        )
        else -> Brush.verticalGradient(
            colors = listOf(
                ElectricBlue.copy(alpha = 0.4f),
                Color(0x330066FF)
            )
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        amplitudes.forEachIndexed { index, amp ->
            val animatedHeightFraction by animateFloatAsState(
                targetValue = amp.coerceIn(0.12f, 1.0f),
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                label = "bar_$index"
            )

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight(animatedHeightFraction)
                    .clip(RoundedCornerShape(2.dp))
                    .background(brush = barBrush)
            )
        }
    }
}
