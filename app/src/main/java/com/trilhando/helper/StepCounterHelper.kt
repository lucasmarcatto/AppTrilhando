package com.trilhando.helper

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

/**
 * Helper para ler o sensor de passos
 * Baixo acoplamento: a Activity só precisa chamar start() e stop().
 */
class StepCounterHelper(
    private val context: Context,
    private val onStepCountChanged: (Int) -> Unit
) : SensorEventListener {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private var stepSensor: Sensor? = null
    private var isListening = false
    private var lastStepCount = 0
    private var initialStepCount = -1 // -1 significa "não definido"

    companion object {
        private const val TAG = "StepCounterHelper"
    }

    //inicia a escuta do sensor de passos
    //retorna true se o sensor estiver disponível
    fun start(): Boolean {
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            Log.e(TAG, "Sensor de passos não disponível neste dispositivo")
            return false
        }

        if (!isListening) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
            isListening = true
            Log.d(TAG, "Escuta do sensor de passos iniciada")
        }
        return true
    }

    //para a escuta do sensor.
    fun stop() {
        if (isListening) {
            sensorManager.unregisterListener(this)
            isListening = false
            Log.d(TAG, "Escuta do sensor de passos parada")
        }
    }

    fun getStepsSinceStart(): Int { //retorna a quantidade de passos desde que o helper foi iniciado
        return if (initialStepCount >= 0) {
            lastStepCount - initialStepCount //Calcula a diferença entre o valor atual e o valor inicial
        } else {
            0
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val steps = event.values[0].toInt()
            Log.d(TAG, "Passos totais (sensor): $steps")

            // Na primeira leitura, define o valor inicial
            if (initialStepCount == -1) {
                initialStepCount = steps
                Log.d(TAG, "Valor inicial de passos definido: $initialStepCount")
            }

            lastStepCount = steps
            val stepsSinceStart = lastStepCount - initialStepCount
            onStepCountChanged(stepsSinceStart)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        Log.d(TAG, "Precisão do sensor alterada: $accuracy")
    }
}