package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PromptCatalog
import com.example.data.model.WorldwidePrompt
import com.example.data.repository.AppRepository
import com.example.ui.components.GlassCard
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
fun DiscoverScreen(
    repository: AppRepository,
    initialSearchQuery: String? = null,
    onUsePromptInChat: (String) -> Unit,
    onUsePromptInDesign: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf(initialSearchQuery ?: "") }
    var selectedCategory by remember { mutableStateOf("All Prompts") }
    var showTrendingOnly by remember { mutableStateOf(false) }

    val savedFavorites by repository.observeFavoritePrompts().collectAsState(initial = emptyList())
    val favoritePromptIds = remember(savedFavorites) { savedFavorites.map { it.promptId }.toSet() }

    // Combine local curated prompts with prompts from Firestore
    val allPrompts = remember { PromptCatalog.curatedPrompts }

    val filteredPrompts = remember(searchQuery, selectedCategory, showTrendingOnly, allPrompts) {
        allPrompts.filter { item ->
            val matchesCategory = selectedCategory == "All Prompts" ||
                    item.category.equals(selectedCategory, ignoreCase = true) ||
                    (selectedCategory == "Transparent PNGs" && item.category.contains("Transparent", ignoreCase = true))

            val matchesSearch = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.prompt.contains(searchQuery, ignoreCase = true) ||
                    item.tags.any { it.contains(searchQuery, ignoreCase = true) }

            val matchesTrending = !showTrendingOnly || item.trending

            matchesCategory && matchesSearch && matchesTrending
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightNavyDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Discover Header
        item {
            Column {
                Text(
                    text = "WORLDWIDE PROMPT LIBRARY",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextWhite,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Discover creative ideas, 3D text & graphics from around the world",
                    fontSize = 12.sp,
                    color = MetallicGold
                )
            }
        }

        // Search Bar & Filter Options
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = ElectricBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text("Search 20+ creative prompt categories...", fontSize = 13.sp, color = TextMuted)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("discover_search_input")
                    )
                }
            }
        }

        // Trending Toggle & 20 Professional Category Filter Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Categories (${PromptCatalog.categories.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSilver
                    )

                    FilterChip(
                        selected = showTrendingOnly,
                        onClick = { showTrendingOnly = !showTrendingOnly },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (showTrendingOnly) MidnightNavyDark else MetallicGold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Trending", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MetallicGold,
                            selectedLabelColor = MidnightNavyDark,
                            containerColor = MidnightNavyElevated,
                            labelColor = MetallicGold
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = showTrendingOnly,
                            borderColor = MetallicGold
                        )
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(PromptCatalog.categories) { cat ->
                        val isSelected = selectedCategory == cat.name
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) ElectricBlue else MidnightNavyElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) Color.White else Color(0x3300D2FF),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedCategory = cat.name }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "${cat.iconEmoji} ${cat.name}",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MidnightNavyDark else TextWhite
                            )
                        }
                    }
                }
            }
        }

        // Prompts Count Header
        item {
            Text(
                text = "${filteredPrompts.size} Inspiration Prompts Found",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextSilver
            )
        }

        // Prompts Cards List
        items(filteredPrompts, key = { it.id }) { promptItem ->
            val isFavorite = favoritePromptIds.contains(promptItem.id)

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                goldAccent = promptItem.trending
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MidnightNavyElevated)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = promptItem.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (promptItem.trending) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x33FFD700))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "🔥 TRENDING",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MetallicGold
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            // Favorite Bookmark Toggle
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        val nowFav = repository.toggleFavoritePrompt(
                                            promptId = promptItem.id,
                                            title = promptItem.title,
                                            category = promptItem.category,
                                            promptText = promptItem.prompt
                                        )
                                        Toast.makeText(
                                            context,
                                            if (nowFav) "Added to Favorites" else "Removed from Favorites",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFavorite) MetallicGold else TextSilver,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = promptItem.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = promptItem.description,
                        fontSize = 12.sp,
                        color = TextMuted,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Full Prompt Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MidnightNavyDark)
                            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = promptItem.prompt,
                            fontSize = 12.sp,
                            color = TextSilver,
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Copy button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MidnightNavyElevated)
                                    .clickable { copyToClipboard(context, promptItem.prompt) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy", fontSize = 11.sp, color = TextWhite)
                                }
                            }

                            // Share button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MidnightNavyElevated)
                                    .clickable { shareText(context, promptItem.prompt) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = TextSilver,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share", fontSize = 11.sp, color = TextWhite)
                                }
                            }
                        }

                        // Use in Studio button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MetallicGold)
                                .clickable {
                                    if (promptItem.category.contains("Transparent", ignoreCase = true) ||
                                        promptItem.category.contains("Typography", ignoreCase = true) ||
                                        promptItem.category.contains("Gold", ignoreCase = true)
                                    ) {
                                        onUsePromptInDesign(promptItem.prompt)
                                    } else {
                                        onUsePromptInChat(promptItem.prompt)
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MidnightNavyDark,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Use Prompt",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MidnightNavyDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
