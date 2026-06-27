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
import com.trilhando.ui.login.LoginActivity
import com.trilhando.ui.settings.SettingsActivity
import com.trilhando.ui.walk.StartWalkActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var tvNome: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnIniciar: Button
    private lateinit var btnHistorico: Button
    private lateinit var btnConfig: Button
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //inicializa as views
        tvNome = findViewById(R.id.tvNomeUsuario)
        tvEmail = findViewById(R.id.tvEmailUsuario)
        btnIniciar = findViewById(R.id.btnIniciarCaminhada)
        btnHistorico = findViewById(R.id.btnHistorico)
        btnConfig = findViewById(R.id.btnConfiguracoes)
        btnLogout = findViewById(R.id.btnLogout)

        // Carregar dados do usuário
        carregarDadosUsuario()

        btnIniciar.setOnClickListener {
            startActivity(Intent(this, StartWalkActivity::class.java))
        }

        btnHistorico.setOnClickListener {
            Toast.makeText(this, "Histórico", Toast.LENGTH_SHORT).show()
        }

        btnConfig.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
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
            startActivity(Intent(this, LoginActivity::class.java)) // Sem usuário logado -> volta pra tela de login
            finish()
            return
        }

        val email = user.email ?: ""
        tvEmail.text = email

        //busca no Firestore
        UserRepository.buscarUsuarioPorEmail(email) { usuario, erro ->
            if (usuario != null) {
                tvNome.text = "Olá, ${usuario.nome}!"
            } else {
                tvNome.text = "Olá, usuário!"
                Toast.makeText(this, "Erro ao carregar perfil: $erro", Toast.LENGTH_SHORT).show()
            }
        }
    }
}