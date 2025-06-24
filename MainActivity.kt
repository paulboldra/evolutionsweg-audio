package com.example.evolutionswegapp

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {

    private lateinit var geofencingClient: com.google.android.gms.location.GeofencingClient
    private lateinit var statusTextView: TextView
    private var mediaPlayer: MediaPlayer? = null

    // Data class to hold the location information
    data class LocationData(val id: String, val lat: Double, val lon: Double, val audioUrl: String)

    // The list of all points of interest for the walk
    private val locations = listOf(
        LocationData("1", 49.339757, 8.761990, "https://evolutionsweg.de/audio/de/location-01.mp3"),
        LocationData("2", 49.342552, 8.759643, "https://evolutionsweg.de/audio/de/location-02.mp3"),
        LocationData("3", 49.345347, 8.757296, "https://evolutionsweg.de/audio/de/location-03.mp3"),
        LocationData("4", 49.348142, 8.754949, "https://evolutionsweg.de/audio/de/location-04.mp3"),
        LocationData("5", 49.350937, 8.752602, "https://evolutionsweg.de/audio/de/location-05.mp3"),
        LocationData("6", 49.353732, 8.750255, "https://evolutionsweg.de/audio/de/location-06.mp3"),
        LocationData("7", 49.356527, 8.747908, "https://evolutionsweg.de/audio/de/location-07.mp3"),
        LocationData("8", 49.359322, 8.745561, "https://evolutionsweg.de/audio/de/location-08.mp3"),
        LocationData("9", 49.362117, 8.743214, "https://evolutionsweg.de/audio/de/location-09.mp3"),
        LocationData("10", 49.364912, 8.740867, "https://evolutionsweg.de/audio/de/location-10.mp3")
    )

    // PendingIntent for the GeofenceBroadcastReceiver
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        )
    }

    // Handles receiving status updates from the GeofenceBroadcastReceiver
    private val statusUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val status = intent?.getStringExtra("status")
            statusTextView.text = "Status: $status"
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        geofencingClient = LocationServices.getGeofencingClient(this)
        statusTextView = findViewById(R.id.statusTextView)
        val startButton: Button = findViewById(R.id.startButton)

        startButton.setOnClickListener {
            checkPermissionsAndStartGeofencing()
        }

        // Register receiver for status updates
        val filter = IntentFilter("GEOFENCE_STATUS_UPDATE")
        registerReceiver(statusUpdateReceiver, filter)
    }

    private fun checkPermissionsAndStartGeofencing() {
        // Check for foreground location permission first
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // If foreground is granted, check for background location on Android 10+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    checkNotificationPermissionAndStart()
                } else {
                    // Request background location permission
                    locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                }
            } else {
                // On older devices, foreground permission is enough
                startGeofencing()
            }
        } else {
            // Request foreground location permission
            locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }
    
    private fun checkNotificationPermissionAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
             if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                 startGeofencing()
             } else {
                 notificationPermissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
             }
        } else {
            startGeofencing()
        }
    }


    private fun startGeofencing() {
        val geofenceList = locations.map { loc ->
            Geofence.Builder()
                .setRequestId(loc.id)
                .setCircularRegion(loc.lat, loc.lon, 50f) // 50-meter radius
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()
        }

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofenceList)
            .build()
        
        // This check is required, but we've already handled permissions.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
             geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                addOnSuccessListener {
                    statusTextView.text = "Status: Geofences added. The walk can begin!"
                    Toast.makeText(this@MainActivity, "Tracking started!", Toast.LENGTH_SHORT).show()
                }
                addOnFailureListener {
                    statusTextView.text = "Status: Failed to add geofences. ${it.message}"
                    Toast.makeText(this@MainActivity, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    // Modern way to handle permission requests
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Fine location granted, now check for background
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                         locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                    } else {
                        checkNotificationPermissionAndStart()
                    }
                } else {
                    startGeofencing()
                }
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_BACKGROUND_LOCATION, false) -> {
                // Background granted
                 checkNotificationPermissionAndStart()
            }
            else -> {
                statusTextView.text = "Status: Location permissions are required to start."
                Toast.makeText(this, "Location access is essential for this app.", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private val notificationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startGeofencing()
        } else {
            Toast.makeText(this, "Notification permission is needed for background tracking alerts.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources
        mediaPlayer?.release()
        mediaPlayer = null
        unregisterReceiver(statusUpdateReceiver)
        // Optional: remove geofences when app is destroyed if desired
        // geofencingClient.removeGeofences(geofencePendingIntent)
    }
}

