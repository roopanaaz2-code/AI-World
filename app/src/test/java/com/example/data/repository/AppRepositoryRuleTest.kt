package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class AppRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createChatSession_authenticatedUser_success() {
        runBlocking {
            signInTestUser(ALICE_EMAIL)
            val repository = AppRepository(firestore)

            val chatId = withTimeout(DEFAULT_TIMEOUT_MS) {
                repository.createChatSession("Test Conversation", "companion", "aria")
            }
            assertTrue(chatId.isNotEmpty())

            val chats = withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeChatSessions().first()
            }
            assertTrue(chats.any { it.id == chatId })
        }
    }

    @Test
    fun addMessage_authenticatedUser_success() {
        runBlocking {
            signInTestUser(ALICE_EMAIL)
            val repository = AppRepository(firestore)

            val chatId = withTimeout(DEFAULT_TIMEOUT_MS) {
                repository.createChatSession("Test Chat", "companion", "aria")
            }

            val msgId = withTimeout(DEFAULT_TIMEOUT_MS) {
                repository.addMessage(chatId, "user", "Hello AI!", "companion")
            }
            assertTrue(msgId.isNotEmpty())

            val messages = withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeMessages(chatId).first()
            }
            assertTrue(messages.any { it.id == msgId })
        }
    }

    @Test
    fun saveCreation_authenticatedUser_success() {
        runBlocking {
            signInTestUser(ALICE_EMAIL)
            val repository = AppRepository(firestore)

            val creationId = withTimeout(DEFAULT_TIMEOUT_MS) {
                repository.saveCreation(
                    title = "Neon Glow Logo",
                    prompt = "Cyberpunk electric blue neon logo",
                    category = "logo",
                    style = "neon",
                    imageData = "data:image/png;base64,sample",
                    isTransparent = true
                )
            }
            assertTrue(creationId.isNotEmpty())

            val creations = withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeCreations().first()
            }
            assertTrue(creations.any { it.id == creationId })
        }
    }

    @Test
    fun unauthenticatedUser_throwsException() {
        runBlocking {
            auth.signOut()
            val repository = AppRepository(firestore)

            assertThrows(IllegalStateException::class.java) {
                runBlocking {
                    repository.createChatSession("Denied Chat", "companion", "aria")
                }
            }
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice_test@example.com"
        const val DEFAULT_TIMEOUT_MS = 6000L
        const val FLOW_TIMEOUT_MS = 6000L
    }
}
