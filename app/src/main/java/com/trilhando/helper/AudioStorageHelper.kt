package com.trilhando.helper

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage

/**
 * Helper de upload de áudio no Firebase Storage.
 * Espelha o PhotoStorageHelper (mesmo padrão de baixo acoplamento).
 */
object AudioStorageHelper {

    private val storage = FirebaseStorage.getInstance()

    // Faz upload do áudio e devolve a URL pública via callback
    fun uploadAudio(userId: String, audioUri: Uri, onComplete: (Boolean, String?) -> Unit) {
        val timestamp = System.currentTimeMillis()
        val ref = storage.reference.child("audios/$userId/$timestamp.m4a")

        ref.putFile(audioUri)
            .continueWithTask { task ->
                if (!task.isSuccessful) throw task.exception!!
                ref.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                onComplete(true, downloadUri.toString())
            }
            .addOnFailureListener { e ->
                onComplete(false, e.message)
            }
    }
}
