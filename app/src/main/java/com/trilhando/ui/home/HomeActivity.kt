package com.trilhando.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.trilhando.R
import com.trilhando.auth.FirebaseAuthHelper
import com.trilhando.ui.login.LoginActivity
import com.trilhando.ui.settings.SettingsActivity
import com.trilhando.ui.walk.StartWalkActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //inicializa as views
        val btnIniciar = findViewById<Button>(R.id.btnIniciarCaminhada)
        val btnHistorico = findViewById<Button>(R.id.btnHistorico)
        val btnConfig = findViewById<Button>(R.id.btnConfiguracoes)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

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
}