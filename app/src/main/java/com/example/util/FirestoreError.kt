package com.example.util

import android.util.Log

enum class OperationType {
    GET, LIST, CREATE, UPDATE, DELETE
}

private const val TAG = "FirestoreError"

fun handleFirestoreError(e: Throwable, op: OperationType, path: String) {
    Log.e(TAG, "[Firestore Error] Operation: $op | Path: $path | Message: ${e.message}", e)
}
