package com.trilhando.ui.walk

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.trilhando.R
import com.trilhando.helper.LocationHelper
import com.trilhando.helper.PermissionHelper
import com.trilhando.helper.StepCounterHelper
import com.trilhando.ui.audio.AudioRecordActivity
import com.trilhando.ui.camera.CameraActivity

class StartWalkActivity : AppCompatActivity() {

    // Views
    private lateinit var tvStatus: TextView
    private lateinit var tvPassos: TextView
    private lateinit var tvPassosInfo: TextView
    private lateinit var tvLocalizacao: TextView
    private lateinit var btnIniciar: Button
    private lateinit var btnParar: Button
    private lateinit var btnFinalizar: Button
    private lateinit var btnAtualizarLocalizacao: Button
    private lateinit var btnTirarFoto: Button
    private lateinit var btnGravarAudio: Button
    private lateinit var btnVoltar: Button

    // Helpers (baixo acoplamento!)
    private lateinit var stepCounterHelper: StepCounterHelper
    private lateinit var locationHelper: LocationHelper

    // Estado da caminhada
    private var isWalking = false
    private var currentLatitude = 0.0
    private var currentLongitude = 0.0
    private var currentSteps = 0
    private var fotoUrl = ""
    private var audioUrl = ""
    private var descricao = ""

    // Recebe a URL da foto quando CameraActivity finaliza
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val url = result.data?.getStringExtra(CameraActivity.EXTRA_FOTO_URL) ?: ""
            if (url.isNotEmpty()) {
                fotoUrl = url
                Toast.makeText(this, "📷 Foto vinculada à caminhada!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Recebe a URL do áudio e a descrição quando AudioRecordActivity finaliza
    private val audioLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val url = result.data?.getStringExtra(AudioRecordActivity.EXTRA_AUDIO_URL) ?: ""
            val desc = result.data?.getStringExtra(AudioRecordActivity.EXTRA_DESCRICAO) ?: ""
            if (url.isNotEmpty()) {
                audioUrl = url
                if (desc.isNotEmpty()) descricao = desc
                Toast.makeText(this, "🎤 Áudio vinculado à caminhada!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_PERMISSION_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_walk)

        initViews()
        initHelpers()
        setupListeners()

        // Verifica permissões ao iniciar
        verificarPermissoes()
    }

    private fun initViews() {
        tvStatus = findViewById(R.id.tvStatus)
        tvPassos = findViewById(R.id.tvPassos)
        tvPassosInfo = findViewById(R.id.tvPassosInfo)
        tvLocalizacao = findViewById(R.id.tvLocalizacao)
        btnIniciar = findViewById(R.id.btnIniciar)
        btnParar = findViewById(R.id.btnParar)
        btnFinalizar = findViewById(R.id.btnFinalizarCaminhada)
        btnAtualizarLocalizacao = findViewById(R.id.btnAtualizarLocalizacao)
        btnTirarFoto = findViewById(R.id.btnTirarFoto)
        btnGravarAudio = findViewById(R.id.btnGravarAudio)
        btnVoltar = findViewById(R.id.btnVoltarHome)

        //estado inicial dos botões de Parar e Finalizar caminhada
        btnParar.isEnabled = false
        btnFinalizar.isEnabled = false
    }

    private fun initHelpers() {  //helper de passos
        stepCounterHelper = StepCounterHelper(this) { steps ->

            currentSteps = steps //Callback atualiza a UI com a contagem

            runOnUiThread { //atualiza as text views
                tvPassos.text = "Passos: $steps"
                tvPassosInfo.text = if (isWalking) "Monitorando..." else "Parado"
            }
        }

        locationHelper = LocationHelper( //helper de localização
            context = this,
            onLocationReceived = { lat, lon ->
                currentLatitude = lat
                currentLongitude = lon
                runOnUiThread {
                    tvLocalizacao.text = "Lat: %.6f\nLon: %.6f".format(lat, lon)
                }
            },
            onError = { mensagem ->
                runOnUiThread {
                    Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun setupListeners() {
        btnIniciar.setOnClickListener {
            iniciarCaminhada()
        }

        btnParar.setOnClickListener {
            pararCaminhada()
        }

        btnFinalizar.setOnClickListener {
            finalizarCaminhada()
        }

        btnTirarFoto.setOnClickListener {
            cameraLauncher.launch(Intent(this, CameraActivity::class.java))
        }

        btnGravarAudio.setOnClickListener {
            audioLauncher.launch(Intent(this, AudioRecordActivity::class.java))
        }

        btnAtualizarLocalizacao.setOnClickListener {
            if (PermissionHelper.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                locationHelper.getCurrentLocation()
            } else {
                verificarPermissoes()
            }
        }

        btnVoltar.setOnClickListener {

            if (isWalking) { //se estiver caminhando, avisa antes de sair
                Toast.makeText(this, "Pare a caminhada antes de sair", Toast.LENGTH_SHORT).show()
            } else {
                finish()
            }
        }
    }

    private fun verificarPermissoes() {
        if (!PermissionHelper.hasAllPermissions(this)) {
            //solicita permissões
            PermissionHelper.requestPermissions(this, REQUEST_PERMISSION_CODE)
        } else { // já tem permissões
            //tenta iniciar o sensor
            iniciarSensorPassos()
            locationHelper.getCurrentLocation() //pega localização inicial

        }
    }

    private fun iniciarSensorPassos() {
        val sensorDisponivel = stepCounterHelper.start()
        if (sensorDisponivel) {
            tvPassosInfo.text = "Sensor pronto!"
        } else {
            tvPassosInfo.text = "Sensor de passos indisponível"
            Toast.makeText(this, "Seu dispositivo não tem sensor de passos", Toast.LENGTH_LONG).show()
        }
    }

    private fun iniciarCaminhada() {
        if (!PermissionHelper.hasAllPermissions(this)) {
            verificarPermissoes()
            return
        }

        if (isWalking) {
            Toast.makeText(this, "Já está caminhando!", Toast.LENGTH_SHORT).show()
            return
        }

        isWalking = true
        tvStatus.text = "▶️ Em andamento"
        tvStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark, theme))
        btnIniciar.isEnabled = false
        btnParar.isEnabled = true
        btnFinalizar.isEnabled = false

        iniciarSensorPassos()
        //stepCounterHelper.start() // inicia o sensor se não estiver rodando
        locationHelper.getCurrentLocation() //pega localização atual

        Toast.makeText(this, "Caminhada iniciada!", Toast.LENGTH_SHORT).show()
    }

    private fun pararCaminhada() {
        if (!isWalking) {
            Toast.makeText(this, "Nenhuma caminhada em andamento", Toast.LENGTH_SHORT).show()
            return
        }

        isWalking = false
        tvStatus.text = "⏹️ Pausado"
        tvStatus.setTextColor(resources.getColor(android.R.color.black, theme))
        btnIniciar.isEnabled = true
        btnParar.isEnabled = false
        btnFinalizar.isEnabled = true

        // para o sensor mas mantém a contagem atual
        stepCounterHelper.stop()

        Toast.makeText(this, "Caminhada pausada", Toast.LENGTH_SHORT).show()
    }

    private fun finalizarCaminhada() {
        if (isWalking) {
            Toast.makeText(this, "Pare a caminhada antes de finalizar", Toast.LENGTH_SHORT).show()
            return
        }

        // pega localização final
        locationHelper.getCurrentLocation()

        // mostra resumo
        val mensagem = """ 
            🏁 Caminhada finalizada!
            Passos: $currentSteps
            Localização: %.6f, %.6f
        """.trimIndent().format(currentLatitude, currentLongitude)

        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()

        // TODO salvar caminhada no Firestore
        //por enquanto só reseta
        resetarEstado()
    }

    private fun resetarEstado() {
        isWalking = false
        currentSteps = 0
        currentLatitude = 0.0
        currentLongitude = 0.0
        fotoUrl = ""
        audioUrl = ""
        descricao = ""

        tvPassos.text = "Passos: 0"
        tvStatus.text = "⏹️ Parado"
        tvStatus.setTextColor(resources.getColor(android.R.color.black, theme))
        tvLocalizacao.text = "Lat: --, Lon: --"
        btnIniciar.isEnabled = true
        btnParar.isEnabled = false
        btnFinalizar.isEnabled = false


        stepCounterHelper.stop()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_PERMISSION_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allGranted) {
                Toast.makeText(this, "Permissões concedidas!", Toast.LENGTH_SHORT).show()
                iniciarSensorPassos()
                locationHelper.getCurrentLocation()
            } else {
                Toast.makeText(
                    this,
                    "Permissões necessárias negadas. Algumas funcionalidades podem não funcionar.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // para o sensor para não gastar bateria
        stepCounterHelper.stop()
    }
}