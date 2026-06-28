package com.trilhando.ui.camera

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.trilhando.R
import com.trilhando.helper.CameraHelper
import com.trilhando.helper.PermissionHelper
import com.trilhando.utils.Base64Helper

class CameraActivity : AppCompatActivity(), CameraHelper.Callback {

    companion object {
        const val EXTRA_CAMINHADA_ID = "caminhada_id"
        const val EXTRA_FOTO_BASE64 = "foto_base64"   // ← mudamos o nome
    }

    private lateinit var ivPreview: ImageView
    private lateinit var tvPreviewHint: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnTirarFoto: Button
    private lateinit var btnSalvar: Button
    private lateinit var btnVoltar: Button

    private lateinit var cameraHelper: CameraHelper
    private var bitmapFoto: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        initViews()
        setupListeners()
        cameraHelper = CameraHelper(this, this)
        verificarPermissao()
    }

    private fun initViews() {
        ivPreview = findViewById(R.id.ivPreview)
        tvPreviewHint = findViewById(R.id.tvPreviewHint)
        tvStatus = findViewById(R.id.tvStatus)
        btnTirarFoto = findViewById(R.id.btnTirarFoto)
        btnSalvar = findViewById(R.id.btnSalvar)
        btnVoltar = findViewById(R.id.btnVoltar)

        btnSalvar.isEnabled = false
    }

    private fun setupListeners() {
        btnTirarFoto.setOnClickListener { tirarFoto() }
        btnSalvar.setOnClickListener { salvarFoto() }
        btnVoltar.setOnClickListener { finish() }
    }

    private fun verificarPermissao() {
        if (!PermissionHelper.hasPermission(this, android.Manifest.permission.CAMERA)) {
            PermissionHelper.requestPermissions(this, 1001)
        }
    }

    private fun tirarFoto() {
        if (!PermissionHelper.hasPermission(this, android.Manifest.permission.CAMERA)) {
            Toast.makeText(this, "Permissão de câmera necessária", Toast.LENGTH_SHORT).show()
            verificarPermissao()
            return
        }
        cameraHelper.tirarFoto()
    }

    // Callback do CameraHelper
    override fun onFotoRecebida(bitmap: Bitmap) {
        // Reduzimos a imagem para evitar Base64 muito grande (opcional)
        val scaled = Bitmap.createScaledBitmap(bitmap, 800, 800, true)
        bitmapFoto = scaled
        ivPreview.setImageBitmap(scaled)
        tvPreviewHint.text = "Foto tirada! Toque em Salvar."
        btnSalvar.isEnabled = true
        tvStatus.text = ""
    }

    // Salvar: converte para Base64 e retorna
    private fun salvarFoto() {
        val bitmap = bitmapFoto
        if (bitmap == null) {
            tvStatus.text = "Erro: Nenhuma foto tirada"
            Toast.makeText(this, "Tire uma foto primeiro", Toast.LENGTH_SHORT).show()
            return
        }

        btnSalvar.isEnabled = false
        tvStatus.text = "Convertendo imagem..."

        // Converte para Base64
        val base64 = Base64Helper.bitmapToBase64(bitmap, quality = 70)

        tvStatus.text = "✅ Pronto!"

        // Devolve a string para quem abriu esta tela
        val retorno = Intent().putExtra(EXTRA_FOTO_BASE64, base64)
        setResult(RESULT_OK, retorno)
        finish()
    }
}