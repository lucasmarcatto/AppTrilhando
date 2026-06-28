package com.trilhando.ui.walk

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.Timestamp
import com.trilhando.R
import com.trilhando.auth.FirebaseAuthHelper
import com.trilhando.helper.LocationHelper
import com.trilhando.helper.PermissionHelper
import com.trilhando.helper.StepCounterHelper
import com.trilhando.model.Caminhada
import com.trilhando.repository.WalkRepository
import com.trilhando.ui.audio.AudioRecordActivity
import com.trilhando.ui.camera.CameraActivity
import com.trilhando.ui.home.HomeActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    private var fotoBase64 = ""
    private var descricao = ""

    // Recebe a URL da foto quando CameraActivity finaliza
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val base64 = result.data?.getStringExtra(CameraActivity.EXTRA_FOTO_BASE64) ?: ""
            if (base64.isNotEmpty()) {
                fotoBase64 = base64
                Toast.makeText(this, "📷 Foto capturada!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Recebe a URL do áudio e a descrição quando AudioRecordActivity finaliza
    private val audioLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val desc = result.data?.getStringExtra(AudioRecordActivity.EXTRA_DESCRICAO) ?: ""
            if (desc.isNotEmpty()) {
                descricao = desc
                Toast.makeText(this, "📝 Descrição: $desc", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_PERMISSION_CODE = 1001
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
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

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
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

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
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

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
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

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun finalizarCaminhada() {
        if (isWalking) {
            Toast.makeText(this, "Pare a caminhada antes de finalizar", Toast.LENGTH_SHORT).show()
            return
        }

        locationHelper.getCurrentLocation()

        val user = FirebaseAuthHelper.getCurrentUser()
        val userId = user?.email ?: run {
            Toast.makeText(this, "Usuário não logado", Toast.LENGTH_SHORT).show()
            return
        }

        // Cria a caminhada com Base64 e descrição
        val caminhada = Caminhada(
            userId = userId,
            titulo = "Caminhada ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}",
            descricao = if (descricao.isNotEmpty()) descricao else "Sem descrição",
            latitude = currentLatitude,
            longitude = currentLongitude,
            quantidadePassos = currentSteps,
            fotoBase64 = fotoBase64,
            dataCriacao = Timestamp.now()
        )

        WalkRepository.salvarCaminhada(caminhada) { sucesso, id ->
            runOnUiThread {
                if (sucesso) {
                    Toast.makeText(this, "✅ Caminhada salva!", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Erro ao salvar: $id", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun resetarEstado() {
        isWalking = false
        currentSteps = 0
        currentLatitude = 0.0
        currentLongitude = 0.0
        fotoBase64 = ""
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

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
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