package com.trilhando.ui.walk

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.trilhando.R

class FinishWalkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish_walk)

        val btnVoltar = findViewById<Button>(R.id.btnVoltarHome)
        btnVoltar.setOnClickListener { finish() }
    }
}