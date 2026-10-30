package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class ChatMessage(
    val id: String = "",
    val userId: String = "",
    val chatId: String = "",
    val sender: String = "user", // "user" or "ai"
    val text: String = "",
    val characterId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
