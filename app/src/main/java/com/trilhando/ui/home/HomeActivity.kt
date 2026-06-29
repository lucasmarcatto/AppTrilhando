package com.trilhando.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.trilhando.R
import com.trilhando.auth.FirebaseAuthHelper
import com.trilhando.repository.UserRepository
import com.trilhando.repository.WalkRepository
import com.trilhando.ui.historico.HistoryActivity
import com.trilhando.ui.login.LoginActivity
import com.trilhando.ui.walk.StartWalkActivity
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.trilhando.helper.PermissionHelper

class HomeActivity : AppCompatActivity() {

    private lateinit var tvNome: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnIniciar: Button
    private lateinit var btnHistorico: Button
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Solicita permissão de notificações
        if (!PermissionHelper.hasAllPermissions(this)) {
            PermissionHelper.requestPermissions(this, 1003)
        }

        //inicializa as views
        tvNome = findViewById(R.id.tvNomeUsuario)
        tvEmail = findViewById(R.id.tvEmailUsuario)
        btnIniciar = findViewById(R.id.btnIniciarCaminhada)
        btnHistorico = findViewById(R.id.btnHistorico)
        btnLogout = findViewById(R.id.btnLogout)

        // Carregar dados do usuário
        carregarDadosUsuario()

        btnIniciar.setOnClickListener {
            startActivity(Intent(this, StartWalkActivity::class.java))
        }

        btnHistorico.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        btnLogout.setOnClickListener {
            FirebaseAuthHelper.signOut()
            startActivity(Intent(this, LoginActivity::class.java)) //volta para a activity de login
            finish()
        }
    }

    private fun carregarDadosUsuario() {
        val user = FirebaseAuthHelper.getCurrentUser()
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val email = user.email ?: ""
        tvEmail.text = email

        // Busca perfil
        UserRepository.buscarUsuarioPorEmail(email) { usuario, erro ->
            if (usuario != null) {
                tvNome.text = "Olá, ${usuario.nome}!"
            } else {
                tvNome.text = "Olá, usuário!"
            }
        }

        // Busca estatísticas
        carregarEstatisticas(email)
    }

    private fun carregarEstatisticas(userId: String) {
        WalkRepository.buscarCaminhadasPorUsuario(userId) { caminhadas, erro ->
            runOnUiThread {
                if (caminhadas != null) {
                    val totalPassos = caminhadas.sumOf { it.quantidadePassos }
                    findViewById<TextView>(R.id.tvTotalPassos).text = totalPassos.toString()
                    findViewById<TextView>(R.id.tvTotalCaminhadas).text = caminhadas.size.toString()
                }
            }
        }
    }
}