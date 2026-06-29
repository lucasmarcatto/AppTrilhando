package com.trilhando.ui.details

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.trilhando.R
import com.trilhando.model.Caminhada
import com.trilhando.utils.Base64Helper
import java.text.SimpleDateFormat
import java.util.Locale

class WalkDetailsActivity : AppCompatActivity() {

    private var caminhada: Caminhada? = null

    private lateinit var tvTitulo: TextView
    private lateinit var tvData: TextView
    private lateinit var tvDescricao: TextView
    private lateinit var tvPassos: TextView
    private lateinit var tvLocalizacao: TextView
    private lateinit var ivFoto: ImageView
    private lateinit var btnVoltar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_walk_details)

        val caminhadaId = intent.getStringExtra("caminhada_id")
        if (caminhadaId.isNullOrEmpty()) {
            Toast.makeText(this, "ID da caminhada não encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        carregarCaminhada(caminhadaId)
    }

    private fun initViews() {
        tvTitulo = findViewById(R.id.tvTitulo)
        tvData = findViewById(R.id.tvData)
        tvDescricao = findViewById(R.id.tvDescricao)
        tvPassos = findViewById(R.id.tvPassos)
        tvLocalizacao = findViewById(R.id.tvLocalizacao)
        ivFoto = findViewById(R.id.ivFoto)
        btnVoltar = findViewById(R.id.btnVoltar)

        btnVoltar.setOnClickListener { finish() }
    }

    private fun carregarCaminhada(id: String) {
        FirebaseFirestore.getInstance()
            .collection("caminhadas")
            .document(id)
            .get()
            .addOnSuccessListener { doc ->
                caminhada = doc.toObject(Caminhada::class.java)
                caminhada?.let {
                    exibirDados(it)
                } ?: run {
                    Toast.makeText(this, "Caminhada não encontrada", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao carregar: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun exibirDados(walk: Caminhada) {  //recebe a caminhada como parâmetro
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        tvTitulo.text = walk.titulo
        tvData.text = dateFormat.format(walk.dataCriacao.toDate())
        tvDescricao.text = "📝 ${walk.descricao}"
        tvPassos.text = "🚶 Passos: ${walk.quantidadePassos}"
        tvLocalizacao.text = "📍 %.6f, %.6f".format(walk.latitude, walk.longitude)

        // Exibe a foto a partir do Base64
        if (walk.fotoBase64.isNotEmpty()) {
            val bitmap = Base64Helper.base64ToBitmap(walk.fotoBase64)
            if (bitmap != null) {
                ivFoto.setImageBitmap(bitmap)
            } else {
                ivFoto.setImageResource(R.drawable.ic_launcher_foreground)
            }
        } else {
            ivFoto.setImageResource(R.drawable.ic_launcher_foreground)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}