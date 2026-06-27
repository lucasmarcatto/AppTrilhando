package com.trilhando.model

//representar o usuário no Firestore
data class Usuario(
    val email: String = "",
    val nome: String = "",
    val fotoPerfil: String = "" // armazenamento de imagem em Base64
)