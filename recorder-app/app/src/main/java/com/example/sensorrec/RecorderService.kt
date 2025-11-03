package com.example.sensorrec

import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.OnNmeaMessageListener
import android.os.Binder
import android.os.IBinder
import android.os.SystemClock
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import sensorrec.FrameOuterClass.Frame
import sensorrec.FrameOuterClass.Header
import sensorrec.FrameOuterClass.LocationRecord
import sensorrec.FrameOuterClass.NmeaRecord
import sensorrec.FrameOuterClass.SensorRecord
import sensorrec.FrameOuterClass.SensorVec3
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

class RecorderService : Service() {
    private val binder = LocalBinder()
    private lateinit var lm: LocationManager
    private lateinit var sm: SensorManager
    private val outPath = "/data/local/tmp/sensorrec_record.bin"
    private val writeQueue = LinkedBlockingQueue<ByteArray>(10000)
    private var writerThreadStarted = false

    override fun onCreate() {
        super.onCreate()
        ProtoUtils.nativeOpenFile(outPath)
        lm = getSystemService(LOCATION_SERVICE) as LocationManager
        sm = getSystemService(SENSOR_SERVICE) as SensorManager
        startWriterThread()
        registerListeners()
    }
    override fun onDestroy() {
        unregisterListeners()
        ProtoUtils.nativeCloseFile()
        super.onDestroy()
    }
    inner class LocalBinder : Binder() { fun getService() = this@RecorderService }
    override fun onBind(intent: Intent?): IBinder? = binder

    private val locListener = object : LocationListener {
        override fun onLocationChanged(loc: Location) {
            val ts = SystemClock.elapsedRealtimeNanos()
            val locRec = LocationRecord.newBuilder()
                .setLatitude(loc.latitude)
                .setLongitude(loc.longitude)
                .setAltitude(loc.altitude)
                .setAccuracy(loc.accuracy)
                .setSpeed(loc.speed)
                .setBearing(loc.bearing)
                .setWallclockMillis(System.currentTimeMillis())
                .build()
            val header = Header.newBuilder().setType(Header.Type.LOCATION).setTimestampNanos(ts).build()
            val frame = Frame.newBuilder().setHeader(header).setLocation(locRec).build()
            enqueueFrame(frame.toByteArray())
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private val nmeaListener = OnNmeaMessageListener { message, timestamp ->
        val ts = SystemClock.elapsedRealtimeNanos()
        val nrec = NmeaRecord.newBuilder().setNmea(message).build()
        val header = Header.newBuilder().setType(Header.Type.NMEA).setTimestampNanos(ts).build()
        val frame = Frame.newBuilder().setHeader(header).setNmea(nrec).build()
        enqueueFrame(frame.toByteArray())
    }

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val ts = event.timestamp
            val sv = SensorVec3.newBuilder().setX(event.values.getOrNull(0)?.toDouble() ?: 0.0)
                .setY(event.values.getOrNull(1)?.toDouble() ?: 0.0)
                .setZ(event.values.getOrNull(2)?.toDouble() ?: 0.0)
                .build()
            val srec = SensorRecord.newBuilder().addValues(sv).setSensorType(event.sensor.type).setAccuracy(event.accuracy).build()
            val type = when (event.sensor.type) {
                Sensor.TYPE_GYROSCOPE -> Header.Type.GYRO
                Sensor.TYPE_ACCELEROMETER -> Header.Type.ACCEL
                Sensor.TYPE_MAGNETIC_FIELD -> Header.Type.MAG
                else -> Header.Type.OTHER
            }
            val header = Header.newBuilder().setType(type).setTimestampNanos(ts).build()
            val frame = Frame.newBuilder().setHeader(header).setSensor(srec).build()
            enqueueFrame(frame.toByteArray())
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private fun registerListeners() {
        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, locListener, null)
            lm.addNmeaListener(nmeaListener)
        } catch (ex: SecurityException) { /* handle */ }
        val types = intArrayOf(Sensor.TYPE_GYROSCOPE, Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_MAGNETIC_FIELD)
        val delay = SensorManager.SENSOR_DELAY_FASTEST
        for (t in types) {
            val s = sm.getDefaultSensor(t) ?: continue
            sm.registerListener(sensorListener, s, delay)
        }
    }

    private fun unregisterListeners() {
        try { lm.removeUpdates(locListener) } catch (_: Exception) {}
        try { lm.removeNmeaListener(nmeaListener) } catch (_: Exception) {}
        sm.unregisterListener(sensorListener)
    }

    private fun enqueueFrame(bytes: ByteArray) {
        writeQueue.offer(bytes)
    }

    private fun startWriterThread() {
        if (writerThreadStarted) return
        writerThreadStarted = true
        thread(start=true) {
            val batch = ArrayList<ByteArray>(128)
            while (true) {
                try {
                    val first = writeQueue.take() // blocks
                    batch.clear()
                    batch.add(first)
                    writeQueue.drainTo(batch, 127)
                    // write each frame via native (prefix handled in native)
                    for (b in batch) {
                        ProtoUtils.nativeWriteFrame(b)
                    }
                } catch (e: InterruptedException) {
                    break
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
        }
    }
}
