package com.trilhando.model

import com.google.firebase.Timestamp

data class Caminhada(
    val id: String = "",                 // ID unico
    val userId: String = "",             // email do usuário
    val titulo: String = "",             // título da caminhada
    val descricao: String = "",
    val latitude: Double = 0.0,          // localização final
    val longitude: Double = 0.0,
    val quantidadePassos: Int = 0,       // contagem de passos
    val fotoUrl: String = "",            // URL da foto no Firebase
    val audioUrl: String = "",           // URL do audio no Firebase
    val dataCriacao: Timestamp = Timestamp.now() // data/hora da caminhada
)