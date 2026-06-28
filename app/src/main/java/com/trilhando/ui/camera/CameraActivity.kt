package com.trilhando.ui.camera

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.trilhando.R
import com.trilhando.helper.PermissionHelper
import com.trilhando.helper.PhotoStorageHelper
import java.io.File

class CameraActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CAMINHADA_ID = "caminhada_id"
        const val EXTRA_FOTO_URL = "foto_url"
    }

    private lateinit var ivPreview: ImageView
    private lateinit var tvPreviewHint: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnTirarFoto: Button
    private lateinit var btnSalvar: Button
    private lateinit var btnVoltar: Button

    private var fotoUri: Uri? = null
    private val caminhadaId: String by lazy {
        intent.getStringExtra(EXTRA_CAMINHADA_ID) ?: ""
    }

    // Launcher moderno (substitui onActivityResult deprecado)
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && fotoUri != null) {
            ivPreview.setImageURI(fotoUri)
            tvPreviewHint.text = "Foto tirada! Toque em Salvar para enviar."
            btnSalvar.isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        initViews()
        setupListeners()
        verificarPermissao()
    }

    private fun initViews() {
        ivPreview = findViewById(R.id.ivPreview)
        tvPreviewHint = findViewById(R.id.tvPreviewHint)
        tvStatus = findViewById(R.id.tvStatus)
        btnTirarFoto = findViewById(R.id.btnTirarFoto)
        btnSalvar = findViewById(R.id.btnSalvar)
        btnVoltar = findViewById(R.id.btnVoltar)
    }

    private fun setupListeners() {
        btnTirarFoto.setOnClickListener { abrirCamera() }
        btnSalvar.setOnClickListener { salvarFoto() }
        btnVoltar.setOnClickListener { finish() }
    }

    private fun verificarPermissao() {
        if (!PermissionHelper.hasPermission(this, android.Manifest.permission.CAMERA)) {
            PermissionHelper.requestPermissions(this, 1001)
        }
    }

    private fun abrirCamera() {
        if (!PermissionHelper.hasPermission(this, android.Manifest.permission.CAMERA)) {
            Toast.makeText(this, "Permissão de câmera necessária", Toast.LENGTH_SHORT).show()
            verificarPermissao()
            return
        }

        // Cria o arquivo onde a câmera vai salvar a foto em alta resolução
        val arquivo = File(externalCacheDir, "foto_${System.currentTimeMillis()}.jpg")
        fotoUri = FileProvider.getUriForFile(this, "com.trilhando.fileprovider", arquivo)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, fotoUri)
        }
        cameraLauncher.launch(intent)
    }

    private fun salvarFoto() {
        val uri = fotoUri ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        btnSalvar.isEnabled = false
        tvStatus.text = "Enviando foto..."

        PhotoStorageHelper.uploadFoto(userId, uri) { sucesso, resultado ->
            runOnUiThread {
                if (sucesso && resultado != null) {
                    tvStatus.text = "✅ Foto salva com sucesso!"

                    // Se veio com ID de caminhada, atualiza o Firestore
                    if (caminhadaId.isNotEmpty()) {
                        atualizarFotoUrlNaCaminhada(caminhadaId, resultado)
                    }

                    // Devolve a URL para quem abriu esta tela
                    val retorno = Intent().putExtra(EXTRA_FOTO_URL, resultado)
                    setResult(RESULT_OK, retorno)
                } else {
                    tvStatus.text = "Erro: $resultado"
                    btnSalvar.isEnabled = true
                }
            }
        }
    }

    // Atualiza apenas o campo fotoUrl na caminhada já existente no Firestore
    private fun atualizarFotoUrlNaCaminhada(caminhadaId: String, fotoUrl: String) {
        FirebaseFirestore.getInstance()
            .collection("caminhadas")
            .document(caminhadaId)
            .update("fotoUrl", fotoUrl)
            .addOnFailureListener { e ->
                runOnUiThread {
                    Toast.makeText(this, "Erro ao vincular foto: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
