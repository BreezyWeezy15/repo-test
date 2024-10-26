package com.app.lockcomposeChild

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FirebaseListenerService : Service() {

    private var firebaseDatabase: DatabaseReference? = null
    private lateinit var valueEventListener: ValueEventListener
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var notificationRunnable: Runnable

    override fun onCreate() {
        super.onCreate()
        showForegroundNotification()
        startNotificationUpdater()
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startFirebaseListener() {
        firebaseDatabase = FirebaseDatabase.getInstance().getReference("Apps")

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
               if(dataSnapshot.exists()){
                   val type = dataSnapshot.child("type").getValue(String::class.java)!!

                   if (type == "new data") {
                       showAppIcon()
                       firebaseDatabase?.child("type")?.setValue("old data")
                   }

               }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseListenerService", "Firebase error: ${databaseError.message}")
            }
        }

        firebaseDatabase!!.addValueEventListener(valueEventListener)
    }



    private fun startNotificationUpdater() {
        notificationRunnable = Runnable {
            startFirebaseListener()
            handler.postDelayed(notificationRunnable, 5000)
        }
        handler.post(notificationRunnable)
    }

    private fun showForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "firebase_listener_channel"
            val channelName = "Firebase Listener Service"
            val channelDescription = "This channel is used by Firebase listener service."
            val importance = NotificationManager.IMPORTANCE_LOW

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, "firebase_listener_channel")
            .setContentTitle("Listening for Firebase Updates")
            .setContentText("The app is listening for updates.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        startForeground(1, notification)
    }

    private fun showAppIcon() {
        val componentName = ComponentName(this, "com.app.lockcomposeChild.MainActivity")
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(notificationRunnable)
        firebaseDatabase?.removeEventListener(valueEventListener)
    }
}


