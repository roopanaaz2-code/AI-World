package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlassBorderBlue
import com.example.ui.theme.GlassBorderGold
import com.example.ui.theme.GlassSurfaceDark
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.MetallicGold

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderGlowColor: Color = GlassBorderBlue,
    isElevated: Boolean = false,
    goldAccent: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val borderColor = when {
        goldAccent -> GlassBorderGold
        else -> borderGlowColor
    }

    val backgroundBrush = if (isElevated) {
        Brush.verticalGradient(
            colors = listOf(
                GlassSurfaceElevated,
                Color(0xEE0B1226)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                GlassSurfaceDark,
                Color(0xDD070B19)
            )
        )
    }

    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isElevated) 12.dp else 6.dp,
                shape = shape,
                ambientColor = if (goldAccent) MetallicGold else ElectricBlue,
                spotColor = if (goldAccent) MetallicGold else ElectricBlue
            )
            .clip(shape)
            .background(brush = backgroundBrush)
            .border(
                border = BorderStroke(1.dp, borderColor),
                shape = shape
            )
            .then(clickModifier)
    ) {
        content()
    }
}
