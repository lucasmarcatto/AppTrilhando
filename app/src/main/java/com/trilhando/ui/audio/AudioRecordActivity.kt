package com.trilhando.ui.audio

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.trilhando.R
import com.trilhando.helper.PermissionHelper
import com.trilhando.helper.SpeechHelper

class AudioRecordActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DESCRICAO = "descricao"
    }

    private lateinit var tvStatus: TextView
    private lateinit var etDescricao: EditText
    private lateinit var btnOuvir: Button
    private lateinit var btnSalvar: Button
    private lateinit var btnVoltar: Button

    private lateinit var speechHelper: SpeechHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_record)

        initViews()
        setupListeners()

        // Verifica permissão do microfone
        verificarPermissaoMicrofone()

        speechHelper = SpeechHelper(
            context = this,
            onResultado = { texto ->
                runOnUiThread {
                    etDescricao.setText(texto)
                    tvStatus.text = "✅ Texto reconhecido!"
                }
            },
            onErro = { erro ->
                runOnUiThread {
                    tvStatus.text = "❌ $erro"
                    Toast.makeText(this, erro, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun initViews() {
        tvStatus = findViewById(R.id.tvStatusAudio)
        etDescricao = findViewById(R.id.etDescricao)
        btnOuvir = findViewById(R.id.btnOuvir)
        btnSalvar = findViewById(R.id.btnSalvar)
        btnVoltar = findViewById(R.id.btnVoltar)

        btnSalvar.isEnabled = false
    }

    private fun setupListeners() {
        btnOuvir.setOnClickListener {
            iniciarReconhecimento()
        }

        btnSalvar.setOnClickListener {
            salvarDescricao()
        }

        btnVoltar.setOnClickListener {
            finish()
        }
    }

    private fun verificarPermissaoMicrofone() {
        if (!PermissionHelper.hasPermission(this, android.Manifest.permission.RECORD_AUDIO)) {
            PermissionHelper.requestPermissions(this, 1002)
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

        tvStatus.text = "🎤 Ouvindo... fale agora"
        btnOuvir.isEnabled = false
        speechHelper.ouvir()
    }

    private fun salvarDescricao() {
        val descricao = etDescricao.text.toString().trim()
        if (descricao.isEmpty()) {
            Toast.makeText(this, "Digite ou fale uma descrição", Toast.LENGTH_SHORT).show()
            return
        }

        // Retorna a descrição para a StartWalkActivity
        val retorno = Intent().putExtra(EXTRA_DESCRICAO, descricao)
        setResult(RESULT_OK, retorno)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        speechHelper.liberar()
    }
}