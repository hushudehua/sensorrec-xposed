package com.example.replay

import android.app.Service
import android.content.Intent
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.IBinder
import android.os.SystemClock
import java.io.File
import kotlin.concurrent.thread
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import sensorrec.FrameOuterClass.Frame
import sensorrec.FrameOuterClass.Header
import sensorrec.FrameOuterClass.LocationRecord
import com.google.protobuf.CodedInputStream

class ReplayService : Service() {
    private val outProvider = "replay_provider"
    private val recordPath = "/sdcard/sensorrec_record.bin" // put the recorded file here
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        try {
            lm.addTestProvider(outProvider, false, false, false, false, true, true, true, Criteria.POWER_LOW, Criteria.ACCURACY_FINE)
            lm.setTestProviderEnabled(outProvider, true)
        } catch (ex: Exception) { ex.printStackTrace() }

        thread {
            try {
                val f = File(recordPath)
                if (!f.exists()) return@thread
                val fis = FileInputStream(f)
                val firstTs = readAndReplay(fis, lm)
                fis.close()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // read frames with [4byte len][protobuf bytes]
    private fun readAndReplay(fis: FileInputStream, lm: LocationManager): Long {
        val buf4 = ByteArray(4)
        var firstRecNs: Long = -1L
        val playStartNs = SystemClock.elapsedRealtimeNanos()
        while (true) {
            val r = fis.read(buf4)
            if (r != 4) break
            val bb = ByteBuffer.wrap(buf4).order(ByteOrder.LITTLE_ENDIAN)
            val l = bb.int
            val data = ByteArray(l)
            var read = 0
            while (read < l) {
                val rr = fis.read(data, read, l - read)
                if (rr < 0) break
                read += rr
            }
            val frame = Frame.parseFrom(data)
            val hdr = frame.header
            val tRec = hdr.timestampNanos
            if (firstRecNs == -1L) firstRecNs = tRec
            val offsetMs = (tRec - firstRecNs) / 1_000_000L
            val targetPlayMs = (playStartNs / 1_000_000L) + offsetMs
            val sleepMs = targetPlayMs - System.currentTimeMillis()
            if (sleepMs > 0) Thread.sleep(sleepMs)
            if (hdr.type == Header.Type.LOCATION && frame.hasLocation()) {
                val loc = frame.location
                val location = Location(outProvider)
                location.latitude = loc.latitude
                location.longitude = loc.longitude
                location.altitude = loc.altitude
                location.accuracy = loc.accuracy
                location.speed = loc.speed
                location.bearing = loc.bearing
                location.time = System.currentTimeMillis()
                try {
                    // set elapsedRealtimeNanos if available via reflection (omitted for brevity)
                    lm.setTestProviderLocation(outProvider, location)
                } catch (ex: SecurityException) { ex.printStackTrace() }
            }
        }
        return firstRecNs
    }
}
