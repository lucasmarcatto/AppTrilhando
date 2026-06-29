package com.trilhando.ui.walk

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.trilhando.R
import com.trilhando.auth.FirebaseAuthHelper
import com.trilhando.helper.PermissionHelper
import com.trilhando.helper.SpeechHelper
import com.trilhando.model.Caminhada
import com.trilhando.repository.WalkRepository
import com.trilhando.ui.home.HomeActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FinishWalkActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PASSOS = "passos"
        const val EXTRA_LATITUDE = "latitude"
        const val EXTRA_LONGITUDE = "longitude"
        const val EXTRA_FOTO_BASE64 = "foto_base64"

        private const val REQUEST_MIC_CODE = 1002
    }

    // Views
    private lateinit var txtPassos: TextView
    private lateinit var txtData: TextView
    private lateinit var edtDescricao: EditText
    private lateinit var btnSpeech: Button
    private lateinit var txtSpeechInfo: TextView
    private lateinit var btnSalvar: Button
    private lateinit var btnDescartar: Button

    // Helper de reconhecimento de voz (baixo acoplamento)
    private lateinit var speechHelper: SpeechHelper

    // Dados recebidos da StartWalkActivity
    private var passos = 0
    private var latitude = 0.0
    private var longitude = 0.0
    private var fotoBase64 = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish_walk)

        receberDados()
        initViews()
        preencherResumo()
        initSpeechHelper()
        setupListeners()

        // Garante a permissão de microfone para a descrição por voz
        verificarPermissaoMicrofone()
    }

    private fun receberDados() {
        passos = intent.getIntExtra(EXTRA_PASSOS, 0)
        latitude = intent.getDoubleExtra(EXTRA_LATITUDE, 0.0)
        longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, 0.0)
        fotoBase64 = intent.getStringExtra(EXTRA_FOTO_BASE64) ?: ""
    }

    private fun initViews() {
        txtPassos = findViewById(R.id.txtPassos)
        txtData = findViewById(R.id.txtData)
        edtDescricao = findViewById(R.id.edtDescricao)
        btnSpeech = findViewById(R.id.btnSpeech)
        txtSpeechInfo = findViewById(R.id.txtSpeechInfo)
        btnSalvar = findViewById(R.id.btnSalvar)
        btnDescartar = findViewById(R.id.btnDescartar)
    }

    private fun preencherResumo() {
        txtPassos.text = "Passos: $passos"
        val dataHoje = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        txtData.text = "Data: $dataHoje"
    }

    private fun initSpeechHelper() {
        speechHelper = SpeechHelper(
            context = this,
            onResultado = { texto ->
                runOnUiThread {
                    edtDescricao.setText(texto)
                    txtSpeechInfo.text = "✅ Texto reconhecido!"
                    btnSpeech.isEnabled = true
                }
            },
            onErro = { erro ->
                runOnUiThread {
                    txtSpeechInfo.text = "❌ $erro"
                    btnSpeech.isEnabled = true
                    Toast.makeText(this, erro, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun setupListeners() {
        btnSpeech.setOnClickListener {
            iniciarReconhecimento()
        }

        btnSalvar.setOnClickListener {
            salvarCaminhada()
        }

        btnDescartar.setOnClickListener {
            irParaHome()
        }
    }

    private fun verificarPermissaoMicrofone() {
        if (!PermissionHelper.hasPermission(this, android.Manifest.permission.RECORD_AUDIO)) {
            PermissionHelper.requestPermissions(this, REQUEST_MIC_CODE)
        }
    }

    private fun iniciarReconhecimento() {
        if (!PermissionHelper.hasPermission(this, android.Manifest.permission.RECORD_AUDIO)) {
            Toast.makeText(this, "Permissão de microfone necessária", Toast.LENGTH_SHORT).show()
            verificarPermissaoMicrofone()
            return
        }

        if (!speechHelper.estaDisponivel()) {
            Toast.makeText(this, "Reconhecimento de voz indisponível", Toast.LENGTH_SHORT).show()
            return
        }

        txtSpeechInfo.text = "🎤 Ouvindo... fale agora"
        btnSpeech.isEnabled = false
        speechHelper.ouvir()
    }

    private fun salvarCaminhada() {
        val user = FirebaseAuthHelper.getCurrentUser()
        val userId = user?.email ?: run {
            Toast.makeText(this, "Usuário não logado", Toast.LENGTH_SHORT).show()
            return
        }

        val descricao = edtDescricao.text.toString().trim()

        // Evita cliques duplos enquanto salva
        btnSalvar.isEnabled = false

        val caminhada = Caminhada(
            userId = userId,
            titulo = "Caminhada ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}",
            descricao = if (descricao.isNotEmpty()) descricao else "Sem descrição",
            latitude = latitude,
            longitude = longitude,
            quantidadePassos = passos,
            fotoBase64 = fotoBase64,
            dataCriacao = Timestamp.now()
        )

        WalkRepository.salvarCaminhada(caminhada) { sucesso, id ->
            runOnUiThread {
                if (sucesso) {
                    Toast.makeText(this, "✅ Caminhada salva!", Toast.LENGTH_LONG).show()
                    irParaHome()
                } else {
                    btnSalvar.isEnabled = true
                    Toast.makeText(this, "Erro ao salvar: $id", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun irParaHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        speechHelper.liberar()
    }
}
