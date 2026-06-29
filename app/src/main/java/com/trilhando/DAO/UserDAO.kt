package com.trilhando.DAO

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.trilhando.model.Usuario

object UserDAO {

    private val db = FirebaseFirestore.getInstance() //referência à coleção no Firestore
    private val TAG = "UserRepository"

    // salva ou atualiza o usuário no Firestore
    //  email como ID do documento para facilitar a busca
    fun salvarUsuario(usuario: Usuario, onComplete: (Boolean, String?) -> Unit) {
        db.collection("usuarios")
            .document(usuario.email) // o documento terá o nome do email
            .set(usuario)            // salva o objeto diretamente
            .addOnSuccessListener {
                Log.d(TAG, "Usuário salvo com sucesso!")
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erro ao salvar usuário: ${e.message}")
                onComplete(false, e.message)
            }
    }

    fun buscarUsuarioPorEmail(email: String, onComplete: (Usuario?, String?) -> Unit) {
        db.collection("usuarios")
            .document(email)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val usuario = document.toObject(Usuario::class.java) // converte o documento para o objeto Usuario
                    onComplete(usuario, null)
                } else {
                    onComplete(null, "Usuário não encontrado")
                }
            }
            .addOnFailureListener { e ->
                onComplete(null, e.message)
            }
    }
}