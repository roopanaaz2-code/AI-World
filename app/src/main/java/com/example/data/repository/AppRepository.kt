package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession
import com.example.data.model.CreationDesign
import com.example.data.model.FavoritePrompt
import com.example.data.model.UserProfile
import com.example.data.model.WorldwidePrompt
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class AppRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- User Profile ---
    fun observeUserProfile(): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) snapshot.toObject(UserProfile::class.java) else null
                }
                .catch { error ->
                    handleFirestoreError(error, OperationType.GET, path)
                    throw error
                }
        )
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        val uid = requireUserId()
        val path = "users/$uid"
        try {
            val docRef = db.collection("users").document(uid)
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to profile.displayName.ifBlank { "Creator" },
                    "email" to (auth.currentUser?.email ?: profile.email),
                    "avatarUrl" to (profile.avatarUrl ?: auth.currentUser?.photoUrl?.toString()),
                    "preferredLanguage" to profile.preferredLanguage,
                    "preferredVoiceId" to profile.preferredVoiceId,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                val updates = mutableMapOf<String, Any>(
                    "displayName" to profile.displayName,
                    "preferredLanguage" to profile.preferredLanguage,
                    "preferredVoiceId" to profile.preferredVoiceId,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                profile.avatarUrl?.let { updates["avatarUrl"] = it }
                docRef.update(updates).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    // --- Chat Sessions ---
    fun observeChatSessions(): Flow<List<ChatSession>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/chats"
        emitAll(
            db.collection("users").document(uid).collection("chats")
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(ChatSession::class.java) }
                .catch { error ->
                    handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createChatSession(title: String, characterId: String, voiceId: String): String {
        val uid = requireUserId()
        val path = "users/$uid/chats"
        try {
            val docRef = db.collection("users").document(uid).collection("chats").document()
            val payload = mapOf(
                "id" to docRef.id,
                "userId" to uid,
                "title" to title,
                "characterId" to characterId,
                "voiceId" to voiceId,
                "lastMessage" to "",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    suspend fun updateChatSession(chatId: String, lastMessage: String, characterId: String? = null, voiceId: String? = null) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("chats").document(chatId)
        try {
            val updates = mutableMapOf<String, Any>(
                "lastMessage" to lastMessage.take(1500),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            characterId?.let { updates["characterId"] = it }
            voiceId?.let { updates["voiceId"] = it }
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteChatSession(chatId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("chats").document(chatId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- Chat Messages ---
    fun observeMessages(chatId: String): Flow<List<ChatMessage>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/chats/$chatId/messages"
        emitAll(
            db.collection("users").document(uid).collection("chats").document(chatId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(ChatMessage::class.java) }
                .catch { error ->
                    handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun addMessage(chatId: String, sender: String, text: String, characterId: String): String {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("chats").document(chatId)
            .collection("messages").document()
        val path = docRef.path
        try {
            val payload = mapOf(
                "id" to docRef.id,
                "userId" to uid,
                "chatId" to chatId,
                "sender" to sender,
                "text" to text,
                "characterId" to characterId,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            // Update parent chat lastMessage
            updateChatSession(chatId, text, characterId)
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    suspend fun deleteMessage(chatId: String, messageId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("chats").document(chatId)
            .collection("messages").document(messageId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- Creations & Transparent Designs ---
    fun observeCreations(): Flow<List<CreationDesign>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/creations"
        emitAll(
            db.collection("users").document(uid).collection("creations")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(CreationDesign::class.java) }
                .catch { error ->
                    handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveCreation(
        title: String,
        prompt: String,
        category: String,
        style: String,
        imageData: String,
        isTransparent: Boolean = true
    ): String {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("creations").document()
        val path = docRef.path
        try {
            val payload = mapOf(
                "id" to docRef.id,
                "userId" to uid,
                "title" to title,
                "prompt" to prompt,
                "category" to category,
                "style" to style,
                "imageData" to imageData,
                "isTransparent" to isTransparent,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    suspend fun deleteCreation(creationId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("creations").document(creationId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- Favorite Prompts ---
    fun observeFavoritePrompts(): Flow<List<FavoritePrompt>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/favorite_prompts"
        emitAll(
            db.collection("users").document(uid).collection("favorite_prompts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(FavoritePrompt::class.java) }
                .catch { error ->
                    handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun toggleFavoritePrompt(promptId: String, title: String, category: String, promptText: String): Boolean {
        val uid = requireUserId()
        val query = db.collection("users").document(uid).collection("favorite_prompts")
            .whereEqualTo("promptId", promptId)
            .get()
            .await()

        return if (!query.isEmpty) {
            for (doc in query.documents) {
                doc.reference.delete().await()
            }
            false // unfavorited
        } else {
            val docRef = db.collection("users").document(uid).collection("favorite_prompts").document()
            val payload = mapOf(
                "id" to docRef.id,
                "userId" to uid,
                "promptId" to promptId,
                "title" to title,
                "category" to category,
                "prompt" to promptText,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            true // favorited
        }
    }

    // --- Worldwide Prompts ---
    fun observeWorldwidePrompts(): Flow<List<WorldwidePrompt>> = flow {
        val path = "prompts"
        emitAll(
            db.collection("prompts")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(WorldwidePrompt::class.java) }
                .catch { error ->
                    handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }
}
