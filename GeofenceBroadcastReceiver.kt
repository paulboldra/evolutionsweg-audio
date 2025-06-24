package com.example.evolutionswegapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private var mediaPlayer: MediaPlayer? = null
    
    // Recreate the locations list here to find the audio URL
    private val locations = listOf(
        MainActivity.LocationData("1", 49.339757, 8.761990, "https://evolutionsweg.de/audio/de/location-01.mp3"),
        MainActivity.LocationData("2", 49.342552, 8.759643, "https://evolutionsweg.de/audio/de/location-02.mp3"),
        MainActivity.LocationData("3", 49.345347, 8.757296, "https://evolutionsweg.de/audio/de/location-03.mp3"),
        MainActivity.LocationData("4", 49.348142, 8.754949, "https://evolutionsweg.de/audio/de/location-04.mp3"),
        MainActivity.LocationData("5", 49.350937, 8.752602, "https://evolutionsweg.de/audio/de/location-05.mp3"),
        MainActivity.LocationData("6", 49.353732, 8.750255, "https://evolutionsweg.de/audio/de/location-06.mp3"),
        MainActivity.LocationData("7", 49.356527, 8.747908, "https://evolutionsweg.de/audio/de/location-07.mp3"),
        MainActivity.LocationData("8", 49.359322, 8.745561, "https://evolutionsweg.de/audio/de/location-08.mp3"),
        MainActivity.LocationData("9", 49.362117, 8.743214, "https://evolutionsweg.de/audio/de/location-09.mp3"),
        MainActivity.LocationData("10", 49.364912, 8.740867, "https://evolutionsweg.de/audio/de/location-10.mp3")
    )

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            // Handle error
            return
        }

        if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            triggeringGeofences.forEach { geofence ->
                val locationId = geofence.requestId
                val locationData = locations.find { it.id == locationId }
                locationData?.let {
                    playAudio(context, it.audioUrl)
                    val statusMessage = "Entered Zone ${it.id}. Playing audio."
                    showNotification(context, "Location Reached!", statusMessage)
                    
                    // Send status update to MainActivity if it's open
                    val statusIntent = Intent("GEOFENCE_STATUS_UPDATE").apply {
                        putExtra("status", statusMessage)
                    }
                    context.sendBroadcast(statusIntent)
                }
            }
        }
    }

    private fun playAudio(context: Context, url: String) {
        // Stop any currently playing audio
        mediaPlayer?.release()
        mediaPlayer = null
        
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(url)
            prepareAsync() // Prepare streaming audio in the background
            setOnPreparedListener {
                it.start()
            }
            setOnCompletionListener {
                it.release()
                mediaPlayer = null
            }
            setOnErrorListener { mp, what, extra ->
                mp.release()
                mediaPlayer = null
                true // Error handled
            }
        }
    }
    
    private fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "geofence_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Geofence Notifications", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Use your app's icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify((0..10000).random(), notification)
    }
}
