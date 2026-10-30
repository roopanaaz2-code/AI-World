package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FavoritePrompt(
    val id: String = "",
    val userId: String = "",
    val promptId: String = "",
    val title: String = "",
    val category: String = "",
    val prompt: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
