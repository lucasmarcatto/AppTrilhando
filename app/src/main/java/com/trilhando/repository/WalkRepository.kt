package com.trilhando.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.trilhando.model.Caminhada

object WalkRepository {

    private val db = FirebaseFirestore.getInstance()

    //salva uma caminhada no Firestore
    fun salvarCaminhada(caminhada: Caminhada, onComplete: (Boolean, String?) -> Unit) {
        db.collection("caminhadas")
            .add(caminhada) // add() gera um ID automático
            .addOnSuccessListener { documentReference ->
                onComplete(true, documentReference.id) //atualiza o objeto com o ID gerado (opcional)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.message)
            }
    }

    //busca todas as caminhadas de um usuario ordenadas pela data
    fun buscarCaminhadasPorUsuario(userId: String, onComplete: (List<Caminhada>?, String?) -> Unit) {
        db.collection("caminhadas")
            .whereEqualTo("userId", userId)
            .orderBy("dataCriacao", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val caminhadas = result.documents.mapNotNull { doc ->
                    doc.toObject(Caminhada::class.java)?.copy(id = doc.id)
                }
                onComplete(caminhadas, null)
            }
            .addOnFailureListener { e ->
                onComplete(null, e.message)
            }
    }
}