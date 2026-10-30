package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class CreationDesign(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val prompt: String = "",
    val category: String = "logo", // "logo", "typography", "sticker", "button", "badge", "transparent_png"
    val style: String = "3d_gold",
    val imageData: String = "", // Base64 encoded PNG
    val isTransparent: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
