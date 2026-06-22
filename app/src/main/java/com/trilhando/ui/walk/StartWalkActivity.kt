package com.trilhando.ui.walk

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.trilhando.R

class StartWalkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_walk)

        val btnVoltar = findViewById<Button>(R.id.btnVoltarHome)
        btnVoltar.setOnClickListener { finish() }
    }
}