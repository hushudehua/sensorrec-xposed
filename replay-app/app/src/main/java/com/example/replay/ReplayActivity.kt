package com.example.replay

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class ReplayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val btn = Button(this)
        btn.text = "Start Replay Service"
        setContentView(btn)
        btn.setOnClickListener {
            startService(Intent(this, ReplayService::class.java))
        }
    }
}
