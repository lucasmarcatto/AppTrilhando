package com.trilhando.helper

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.ToneGenerator
import android.net.Uri
import android.util.Log
import java.io.File

/**
 * Helper para gravar e reproduzir áudio.
 * Baixo acoplamento: a Activity só chama iniciarGravacao(), pararGravacao(), reproduzir().
 */
class AudioRecorderHelper(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var arquivoAtual: File? = null

    companion object {
        private const val TAG = "AudioRecorderHelper"
    }

    /**
     * Inicia a gravação. Cria um arquivo .m4a no cache e devolve seu caminho.
     * Toca um "beep" de feedback ao começar.
     */
    fun iniciarGravacao(): Boolean {
        return try {
            val arquivo = File(context.externalCacheDir, "audio_${System.currentTimeMillis()}.m4a")
            arquivoAtual = arquivo

            // A partir do Android 12 (API 31) usa-se o construtor com Context
            recorder = MediaRecorder(context).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(arquivo.absolutePath)
                prepare()
                start()
            }

            tocarBeep(ToneGenerator.TONE_PROP_BEEP) // som de feedback ao iniciar
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao iniciar gravação: ${e.message}")
            false
        }
    }

    /**
     * Para a gravação e devolve a Uri do arquivo gravado (ou null em caso de erro).
     */
    fun pararGravacao(): Uri? {
        return try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null

            tocarBeep(ToneGenerator.TONE_PROP_BEEP2) // som de feedback ao parar
            arquivoAtual?.let { Uri.fromFile(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao parar gravação: ${e.message}")
            recorder = null
            null
        }
    }

    /**
     * Reproduz o último áudio gravado. onComplete é chamado quando a reprodução termina.
     */
    fun reproduzir(onComplete: () -> Unit = {}) {
        val arquivo = arquivoAtual ?: return

        pararReproducao() // garante que não há outro tocando

        player = MediaPlayer().apply {
            setDataSource(arquivo.absolutePath)
            prepare()
            start()
            setOnCompletionListener {
                onComplete()
            }
        }
    }

    fun pararReproducao() {
        player?.release()
        player = null
    }

    /** Indica se há um áudio gravado disponível. */
    fun temAudio(): Boolean = arquivoAtual?.exists() == true

    /** Libera todos os recursos (chamar no onDestroy da Activity). */
    fun liberar() {
        recorder?.release()
        recorder = null
        pararReproducao()
    }

    // Toca um beep curto de feedback sonoro (RF03)
    private fun tocarBeep(tipo: Int) {
        try {
            val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
            tone.startTone(tipo, 200)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao tocar beep: ${e.message}")
        }
    }
}
