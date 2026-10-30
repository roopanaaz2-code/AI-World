package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class WorldwidePrompt(
    val id: String = "",
    val title: String = "",
    val prompt: String = "",
    val category: String = "All",
    val description: String = "",
    val aiTool: String = "Gemini",
    val trending: Boolean = false,
    val tags: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
)
