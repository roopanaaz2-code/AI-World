package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.repository.AppRepository
import com.example.data.service.AiAssistantService
import com.example.data.service.DesignOptions
import com.example.data.service.DesignStyle
import com.example.data.service.GraphicDesignEngine
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingButton
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MetallicGold
import com.example.ui.theme.MidnightNavyDark
import com.example.ui.theme.MidnightNavyElevated
import com.example.ui.theme.MidnightNavySurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSilver
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.launch

@Composable
fun DesignStudioScreen(
    initialPrompt: String? = null,
    repository: AppRepository,
    aiService: AiAssistantService
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var textInput by remember { mutableStateOf(initialPrompt ?: "CREATIVE") }
    var subtextInput by remember { mutableStateOf("STUDIO") }
    var selectedStyle by remember { mutableStateOf(DesignStyle.GOLD_3D) }

    var glowIntensity by remember { mutableStateOf(1.0f) }
    var depth3D by remember { mutableStateOf(8) }
    var outlineWidth by remember { mutableStateOf(4f) }

    var showCheckerboardBackdrop by remember { mutableStateOf(true) }
    var renderedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    fun regenerateDesign() {
        val options = DesignOptions(
            text = textInput.ifBlank { "STUDIO" },
            subtext = subtextInput,
            style = selectedStyle,
            glowIntensity = glowIntensity,
            depth3D = depth3D,
            outlineWidth = outlineWidth
        )
        renderedBitmap = GraphicDesignEngine.renderTransparentDesign(options)
    }

    LaunchedEffect(textInput, subtextInput, selectedStyle, glowIntensity, depth3D, outlineWidth) {
        regenerateDesign()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightNavyDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "DESIGN STUDIO",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextWhite,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Transparent PNG Graphic & 3D Typography Studio",
                    fontSize = 12.sp,
                    color = MetallicGold
                )
            }
        }

        // Live Render Preview Canvas
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                isElevated = true,
                goldAccent = selectedStyle == DesignStyle.GOLD_3D
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live Canvas (Alpha Channel)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )

                        // Toggle checkerboard background to visualize real transparency
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MidnightNavyElevated)
                                .clickable { showCheckerboardBackdrop = !showCheckerboardBackdrop }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridOn,
                                contentDescription = null,
                                tint = if (showCheckerboardBackdrop) ElectricBlue else TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showCheckerboardBackdrop) "Grid On" else "Solid",
                                fontSize = 11.sp,
                                color = TextSilver
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Viewport Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (showCheckerboardBackdrop) Color(0xFF131B33) else MidnightNavyDark
                            )
                            .border(1.dp, Color(0x3300D2FF), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        renderedBitmap?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Generated Transparent PNG",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                contentScale = ContentScale.Fit
                            )
                        } ?: run {
                            CircularProgressIndicator(color = ElectricBlue)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons: Save to Creations & Share
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlowingButton(
                            text = if (isSaving) "Saving..." else "Save PNG",
                            onClick = {
                                renderedBitmap?.let { bmp ->
                                    isSaving = true
                                    coroutineScope.launch {
                                        try {
                                            val base64 = GraphicDesignEngine.toBase64Png(bmp)
                                            repository.saveCreation(
                                                title = textInput.ifBlank { "Custom Design" },
                                                prompt = "$textInput ($subtextInput) - ${selectedStyle.displayName}",
                                                category = when (selectedStyle) {
                                                    DesignStyle.GOLD_3D -> "3D typography"
                                                    DesignStyle.ELECTRIC_NEON -> "neon typography"
                                                    DesignStyle.GLOWING_BUTTON -> "button"
                                                    DesignStyle.GAMING_STICKER -> "sticker"
                                                    else -> "logo"
                                                },
                                                style = selectedStyle.name,
                                                imageData = base64,
                                                isTransparent = true
                                            )
                                            isSaving = false
                                            Toast.makeText(context, "Saved to My Creations!", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            isSaving = false
                                            Toast.makeText(context, "Error saving: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            icon = Icons.Default.CloudUpload,
                            isLoading = isSaving,
                            modifier = Modifier.weight(1f),
                            testTag = "design_save_button"
                        )

                        GlowingButton(
                            text = "Export & Share",
                            onClick = {
                                renderedBitmap?.let { bmp ->
                                    shareTransparentPng(context, bmp, textInput)
                                }
                            },
                            icon = Icons.Default.Share,
                            isGold = true,
                            modifier = Modifier.weight(1f),
                            testTag = "design_share_button"
                        )
                    }
                }
            }
        }

        // Text Content Inputs
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Design Text Content",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        label = { Text("Primary Headline Text", color = TextSilver) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = subtextInput,
                        onValueChange = { subtextInput = it },
                        label = { Text("Subtext / Tagline (Optional)", color = TextSilver) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MetallicGold,
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        // 6 Design Styles Selector
        item {
            Column {
                Text(
                    text = "Select Graphic Style",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSilver
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(DesignStyle.values()) { style ->
                        val isSelected = style == selectedStyle
                        GlassCard(
                            modifier = Modifier
                                .width(160.dp)
                                .clickable { selectedStyle = style },
                            goldAccent = isSelected && style == DesignStyle.GOLD_3D,
                            borderGlowColor = if (isSelected) ElectricBlue else Color(0x22FFFFFF)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = style.displayName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MetallicGold else TextWhite
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = style.description,
                                    fontSize = 10.sp,
                                    color = TextMuted,
                                    maxLines = 2,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Visual Sliders & Attributes
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Attributes & Depth Controls",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Glow Intensity
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Glow Intensity", fontSize = 12.sp, color = TextSilver)
                        Text(text = "${String.format("%.1f", glowIntensity)}x", fontSize = 12.sp, color = ElectricBlue)
                    }
                    Slider(
                        value = glowIntensity,
                        onValueChange = { glowIntensity = it },
                        valueRange = 0.2f..2.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricBlue,
                            activeTrackColor = ElectricBlue,
                            inactiveTrackColor = MidnightNavyElevated
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3D Depth
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "3D Extrusion Depth", fontSize = 12.sp, color = TextSilver)
                        Text(text = "${depth3D}px", fontSize = 12.sp, color = MetallicGold)
                    }
                    Slider(
                        value = depth3D.toFloat(),
                        onValueChange = { depth3D = it.toInt() },
                        valueRange = 1f..16f,
                        colors = SliderDefaults.colors(
                            thumbColor = MetallicGold,
                            activeTrackColor = MetallicGold,
                            inactiveTrackColor = MidnightNavyElevated
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Outline Width
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Outline Stroke Width", fontSize = 12.sp, color = TextSilver)
                        Text(text = "${String.format("%.1f", outlineWidth)}px", fontSize = 12.sp, color = Color.White)
                    }
                    Slider(
                        value = outlineWidth,
                        onValueChange = { outlineWidth = it },
                        valueRange = 0f..10f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = MidnightNavyElevated
                        )
                    )
                }
            }
        }
    }
}

fun shareTransparentPng(context: Context, bitmap: Bitmap, title: String) {
    try {
        val file = GraphicDesignEngine.saveToCache(context, bitmap, "transparent_${System.currentTimeMillis()}.png")
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Transparent PNG"))
    } catch (e: Exception) {
        // Fallback standard share
        Toast.makeText(context, "Saved PNG to device cache", Toast.LENGTH_SHORT).show()
    }
}
