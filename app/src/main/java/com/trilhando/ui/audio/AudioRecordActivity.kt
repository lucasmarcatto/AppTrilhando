package com.trilhando.ui.audio

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.trilhando.R
import com.trilhando.helper.AudioRecorderHelper
import com.trilhando.helper.AudioStorageHelper
import com.trilhando.helper.PermissionHelper
import com.trilhando.helper.SpeechHelper

class AudioRecordActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CAMINHADA_ID = "caminhada_id"
        const val EXTRA_AUDIO_URL = "audio_url"
        const val EXTRA_DESCRICAO = "descricao"
        private const val REQUEST_PERMISSION_CODE = 2001
    }

    // Views
    private lateinit var tvStatus: TextView
    private lateinit var etDescricao: EditText
    private lateinit var btnGravar: Button
    private lateinit var btnParar: Button
    private lateinit var btnReproduzir: Button
    private lateinit var btnFalar: Button
    private lateinit var btnSalvar: Button
    private lateinit var btnVoltar: Button

    // Helpers (baixo acoplamento)
    private lateinit var audioHelper: AudioRecorderHelper
    private lateinit var speechHelper: SpeechHelper

    private var audioUri: Uri? = null
    private val caminhadaId: String by lazy {
        intent.getStringExtra(EXTRA_CAMINHADA_ID) ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_record)

        initViews()
        initHelpers()
        setupListeners()
        verificarPermissao()
    }

    private fun initViews() {
        tvStatus = findViewById(R.id.tvStatus)
        etDescricao = findViewById(R.id.etDescricao)
        btnGravar = findViewById(R.id.btnGravar)
        btnParar = findViewById(R.id.btnParar)
        btnReproduzir = findViewById(R.id.btnReproduzir)
        btnFalar = findViewById(R.id.btnFalar)
        btnSalvar = findViewById(R.id.btnSalvar)
        btnVoltar = findViewById(R.id.btnVoltar)
    }

    private fun initHelpers() {
        audioHelper = AudioRecorderHelper(this)

        speechHelper = SpeechHelper(
            context = this,
            onResultado = { texto ->
                runOnUiThread {
                    // Acrescenta o texto reconhecido ao campo
                    val atual = etDescricao.text.toString()
                    etDescricao.setText(if (atual.isEmpty()) texto else "$atual $texto")
                    tvStatus.text = "Texto reconhecido!"
                }
            },
            onErro = { mensagem ->
                runOnUiThread {
                    Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun setupListeners() {
        btnGravar.setOnClickListener { gravar() }
        btnParar.setOnClickListener { parar() }
        btnReproduzir.setOnClickListener { reproduzir() }
        btnFalar.setOnClickListener { falar() }
        btnSalvar.setOnClickListener { salvar() }
        btnVoltar.setOnClickListener { finish() }
    }

    private fun verificarPermissao() {
        if (!PermissionHelper.hasPermission(this, android.Manifest.permission.RECORD_AUDIO)) {
            PermissionHelper.requestPermissions(this, REQUEST_PERMISSION_CODE)
        }
    }

    private fun gravar() {
        if (!PermissionHelper.hasPermission(this, android.Manifest.permission.RECORD_AUDIO)) {
            Toast.makeText(this, "Permissão de microfone necessária", Toast.LENGTH_SHORT).show()
            verificarPermissao()
            return
        }

        if (audioHelper.iniciarGravacao()) {
            tvStatus.text = "🔴 Gravando..."
            btnGravar.isEnabled = false
            btnParar.isEnabled = true
            btnReproduzir.isEnabled = false
            btnSalvar.isEnabled = false
        } else {
            Toast.makeText(this, "Erro ao iniciar gravação", Toast.LENGTH_SHORT).show()
        }
    }

    private fun parar() {
        audioUri = audioHelper.pararGravacao()
        tvStatus.text = if (audioUri != null) "Gravação concluída!" else "Erro na gravação"

        btnGravar.isEnabled = true
        btnParar.isEnabled = false
        btnReproduzir.isEnabled = audioUri != null
        btnSalvar.isEnabled = audioUri != null
    }

    private fun reproduzir() {
        tvStatus.text = "▶️ Reproduzindo..."
        audioHelper.reproduzir {
            runOnUiThread { tvStatus.text = "Reprodução concluída" }
        }
    }

    private fun falar() {
        tvStatus.text = "🎙️ Ouvindo..."
        speechHelper.ouvir()
    }

    private fun salvar() {
        val uri = audioUri ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        btnSalvar.isEnabled = false
        tvStatus.text = "Enviando áudio..."

        AudioStorageHelper.uploadAudio(userId, uri) { sucesso, resultado ->
            runOnUiThread {
                if (sucesso && resultado != null) {
                    tvStatus.text = "✅ Áudio salvo com sucesso!"

                    // Atualiza o Firestore se veio com ID de caminhada
                    if (caminhadaId.isNotEmpty()) {
                        atualizarCaminhada(caminhadaId, resultado, etDescricao.text.toString())
                    }

                    // Devolve a URL e a descrição para quem abriu a tela
                    val retorno = Intent()
                        .putExtra(EXTRA_AUDIO_URL, resultado)
                        .putExtra(EXTRA_DESCRICAO, etDescricao.text.toString())
                    setResult(RESULT_OK, retorno)
                } else {
                    tvStatus.text = "Erro: $resultado"
                    btnSalvar.isEnabled = true
                }
            }
        }
    }

    // Atualiza audioUrl e descricao na caminhada já existente no Firestore
    private fun atualizarCaminhada(caminhadaId: String, audioUrl: String, descricao: String) {
        FirebaseFirestore.getInstance()
            .collection("caminhadas")
            .document(caminhadaId)
            .update(mapOf("audioUrl" to audioUrl, "descricao" to descricao))
            .addOnFailureListener { e ->
                runOnUiThread {
                    Toast.makeText(this, "Erro ao vincular áudio: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            val concedida = grantResults.isNotEmpty() &&
                grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!concedida) {
                Toast.makeText(this, "Permissão de microfone negada", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioHelper.liberar()
        speechHelper.liberar()
    }
}
