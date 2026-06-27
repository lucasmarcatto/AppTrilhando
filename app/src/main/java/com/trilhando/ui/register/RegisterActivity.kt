package com.trilhando.ui.register

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.trilhando.R
import com.trilhando.auth.FirebaseAuthHelper
import com.trilhando.model.Usuario
import com.trilhando.repository.UserRepository
import com.trilhando.ui.home.HomeActivity
import com.trilhando.ui.login.LoginActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var etNome: EditText
    private lateinit var etEmail: EditText
    private lateinit var etSenha: EditText
    private lateinit var etConfirmar: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var btnVoltar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //inicializa as views
        etNome = findViewById(R.id.etNome)
        etEmail = findViewById(R.id.etEmailReg)
        etSenha = findViewById(R.id.etSenhaReg)
        etConfirmar = findViewById(R.id.etConfirmarSenha)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        btnVoltar = findViewById(R.id.btnVoltarLogin)

        btnRegistrar.setOnClickListener {
            realizarCadastro()
        }

        btnVoltar.setOnClickListener {
            finish()
        }
    }

    private fun realizarCadastro() {
        val nome = etNome.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val senha = etSenha.text.toString().trim()
        val confirmar = etConfirmar.text.toString().trim()

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confirmar.isEmpty()) { //validação
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (senha.length < 6) { //validaçao
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        if (senha != confirmar) { //validação
            Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
            return
        }

        btnRegistrar.isEnabled = false

        FirebaseAuthHelper.signUp(email, senha) { sucesso, mensagem -> // criar usuário no Firebase Authentication
            if (sucesso) {
                val usuario = Usuario( // salvar dados do perfil no Firestore
                    email = email,
                    nome = nome,
                    fotoPerfil = "" // por enquanto vazio, depois adicionamos foto
                )

                UserRepository.salvarUsuario(usuario) { salvou, erro ->
                    if (salvou) {
                        Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java)) //direciona para Home após cadastro e login automatico
                        finish()
                    } else {
                        Toast.makeText(this, "Erro ao salvar perfil: $erro", Toast.LENGTH_LONG).show()
                        btnRegistrar.isEnabled = true
                    }
                }
            } else {
                Toast.makeText(this, "Erro no cadastro: $mensagem", Toast.LENGTH_LONG).show()
                btnRegistrar.isEnabled = true
            }
        }
    }
}

//Após criar a conta no Authentication (que já loga automaticamente), salvamos o objeto Usuario no Firestore usando o UserRepository. O e-mail é a chave do documento.