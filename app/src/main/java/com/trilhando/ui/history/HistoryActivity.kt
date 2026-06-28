package com.trilhando.ui.historico

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trilhando.R
import com.trilhando.adapter.WalkAdapter
import com.trilhando.auth.FirebaseAuthHelper
import com.trilhando.repository.WalkRepository
import com.trilhando.ui.details.WalkDetailsActivity

class HistoryActivity : AppCompatActivity() {

    private lateinit var rvHistorico: RecyclerView
    private lateinit var btnVoltar: Button
    private lateinit var adapter: WalkAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        rvHistorico = findViewById(R.id.rvHistory)
        btnVoltar = findViewById(R.id.btnVoltar)

        rvHistorico.layoutManager = LinearLayoutManager(this)

        adapter = WalkAdapter(emptyList()) { caminhada ->
            // Ao clicar em um item, abre a tela de detalhes
            val intent = Intent(this, WalkDetailsActivity::class.java)
            intent.putExtra("caminhada_id", caminhada.id)
            startActivity(intent)
        }
        rvHistorico.adapter = adapter

        btnVoltar.setOnClickListener { finish() }

        carregarCaminhadas()
    }

    private fun carregarCaminhadas() {
        val user = FirebaseAuthHelper.getCurrentUser()
        val userId = user?.email ?: return

        WalkRepository.buscarCaminhadasPorUsuario(userId) { caminhadas, erro ->
            runOnUiThread {
                if (caminhadas != null) {
                    adapter.updateList(caminhadas)
                } else {
                    Toast.makeText(this, "Erro ao carregar: $erro", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}