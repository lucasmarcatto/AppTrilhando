package com.trilhando.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.trilhando.R
import com.trilhando.auth.FirebaseAuthHelper
import com.trilhando.ui.home.HomeActivity
import com.trilhando.ui.register.RegisterActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etSenha: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnIrRegistro: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //inicializa as views
        etEmail = findViewById(R.id.etEmail)
        etSenha = findViewById(R.id.etSenha)
        btnLogin = findViewById(R.id.btnLogin)
        btnIrRegistro = findViewById(R.id.btnIrRegistro)


        if (FirebaseAuthHelper.isUserLoggedIn()) { //verifica se o usuario já está logado
            irParaHome()
            return // não executa o resto do onCreate
        }

        btnLogin.setOnClickListener {
            realizarLogin()
        }

        btnIrRegistro.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun realizarLogin() {
        val email = etEmail.text.toString().trim()
        val senha = etSenha.text.toString().trim()

        if (email.isEmpty() || senha.isEmpty()) { //validação de preenchimento
            Toast.makeText(this, "Preencha e-mail e senha", Toast.LENGTH_SHORT).show()
            return
        }

        if (senha.length < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuthHelper.signIn(email, senha) { sucesso, mensagem -> //chama o helper do firebase
            if (sucesso) {
                irParaHome()
            } else {
                Toast.makeText(this, "Erro no login: $mensagem", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun irParaHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish() //encerra LoginActivity para o usuario nao voltar com o botão "voltar"
    }
}