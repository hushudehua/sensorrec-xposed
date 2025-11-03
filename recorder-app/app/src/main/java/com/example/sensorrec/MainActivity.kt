package com.example.sensorrec

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.provider.Settings
import android.content.Context
import android.Manifest
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

class MainActivity : AppCompatActivity() {
    val REQ_PERM = 1234
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val btn = Button(this)
        btn.text = "Start Recorder Service"
        setContentView(btn)
        btn.setOnClickListener {
            val perms = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BODY_SENSORS)
            ActivityCompat.requestPermissions(this, perms, REQ_PERM)
            startService(Intent(this, RecorderService::class.java))
        }
    }
    override fun onRequestPermissionsResult(req:Int, perms:Array<String>, results:IntArray) {
        super.onRequestPermissionsResult(req, perms, results)
    }
}
