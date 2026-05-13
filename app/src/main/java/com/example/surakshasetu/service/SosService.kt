package com.example.surakshasetu.service

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Location
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.surakshasetu.data.repository.SosRepository
import com.example.surakshasetu.util.ShakeDetector
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class SosService : Service() {

    @Inject
    lateinit var sosRepository: SosRepository
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var mediaRecorder: MediaRecorder? = null
    
    private lateinit var sensorManager: SensorManager
    private var shakeDetector: ShakeDetector? = null
    
    private var currentUid: String? = null
    private var currentUserName: String? = null
    private var isSosTriggered = false

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        shakeDetector = ShakeDetector {
            Log.d("SosService", "Shake detected!")
            if (!isSosTriggered && currentUid != null) {
                triggerSos(currentUid!!, currentUserName ?: "User")
            }
        }
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val uid = intent?.getStringExtra("uid")
        val userName = intent.getStringExtra("userName")
        
        if (uid != null) {
            currentUid = uid
            currentUserName = userName
        }

        startForegroundService()
        
        // If the intent specifically asked to trigger SOS (e.g. from button click)
        if (intent?.action == ACTION_TRIGGER_SOS && uid != null) {
            triggerSos(uid, userName ?: "User")
        }
        
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "sos_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "SOS Service",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Suraksha Setu Active")
            .setContentText(if (isSosTriggered) "SOS Alert Active! Emergency services notified." else "Shake your phone to trigger an emergency alert.")
            .setSmallIcon(android.R.drawable.ic_menu_help)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            var type = ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                type = type or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            }
            startForeground(1, notification, type)
        } else {
            startForeground(1, notification)
        }
    }

    private fun triggerSos(uid: String, userName: String) {
        if (isSosTriggered) return
        isSosTriggered = true
        
        // Update notification to show alert state
        startForegroundService()

        serviceScope.launch {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@SosService)
                if (ActivityCompat.checkSelfPermission(
                        this@SosService,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                        this@SosService,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                        location?.let {
                            launch {
                                try {
                                    sosRepository.triggerSos(uid, userName, it.latitude, it.longitude)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                }
                
                startRecording(uid)
                delay(30000) // Record for 30 seconds
                stopRecording()
                
                isSosTriggered = false
                // Reset notification back to monitoring state
                startForegroundService()
                
            } catch (e: Exception) {
                e.printStackTrace()
                isSosTriggered = false
                startForegroundService()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun startRecording(uid: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val file = File(cacheDir, "sos_audio.3gp")
        try {
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            
            serviceScope.launch {
                delay(31000)
                if (file.exists()) {
                    try {
                        sosRepository.uploadSosAudio(uid, file)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaRecorder = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(shakeDetector)
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_TRIGGER_SOS = "com.example.surakshasetu.ACTION_TRIGGER_SOS"
    }
}
