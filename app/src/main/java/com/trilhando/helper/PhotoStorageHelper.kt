package com.trilhando.helper

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage

object PhotoStorageHelper {

    private val storage = FirebaseStorage.getInstance()

    // Faz upload da foto e devolve a URL pública via callback
    fun uploadFoto(userId: String, fotoUri: Uri, onComplete: (Boolean, String?) -> Unit) {
        val timestamp = System.currentTimeMillis()
        val ref = storage.reference.child("caminhadas/$userId/$timestamp.jpg")

        ref.putFile(fotoUri)
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
