package com.trilhando.helper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

/**
 * Helper para reconhecimento de voz (voz -> texto).
 * Baixo acoplamento: a Activity só chama ouvir() e recebe o texto pelo callback.
 *
 * Observação: o SpeechRecognizer NÃO funciona em emulador, apenas em dispositivo físico.
 */
class SpeechHelper(
    private val context: Context,
    private val onResultado: (String) -> Unit,
    private val onErro: (String) -> Unit
) {

    private var recognizer: SpeechRecognizer? = null

    companion object {
        private const val TAG = "SpeechHelper"
    }

    fun estaDisponivel(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    /** Inicia a escuta. O texto reconhecido volta em onResultado. */
    fun ouvir() {
        if (!estaDisponivel()) {
            onErro("Reconhecimento de voz indisponível neste dispositivo")
            return
        }

        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(listener)
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }

        recognizer?.startListening(intent)
    }

    fun parar() {
        recognizer?.stopListening()
    }

    fun liberar() {
        recognizer?.destroy()
        recognizer = null
    }

    private val listener = object : RecognitionListener {
        override fun onResults(results: Bundle?) {
            val lista = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val texto = lista?.firstOrNull()
            if (!texto.isNullOrEmpty()) {
                onResultado(texto)
            } else {
                onErro("Nada reconhecido")
            }
        }

        override fun onError(error: Int) {
            Log.e(TAG, "Erro no reconhecimento: código $error")
            onErro("Erro ao reconhecer voz (código $error)")
        }

        // Métodos obrigatórios da interface (não usados aqui)
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}
