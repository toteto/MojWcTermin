package com.example.mojwctermin

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import mu.KLogging

class RestroomAvailabilityService : Service() {
    override fun onBind(p0: Intent?): IBinder {
        throw UnsupportedOperationException()
    }

    private val mDatabase by lazy { FirebaseDatabase.getInstance() }
    private val restroomReference by lazy { mDatabase.getReference("mRestroomDoorClosed") }


    override fun onCreate() {
        super.onCreate()
        started = true
        logger.debug("onCreate called with.")
        restroomReference.addValueEventListener(doorClosedStateListener)
        RestroomAvailabilityRepository.subscribe(localAvailabilityChangeListener)
        updateNotification(RestroomAvailabilityRepository.restroomAvailable)
    }

    private val doorClosedStateListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError?) {
            logger.error("Fetching of availability canceled: ${p0?.message}.")
        }

        override fun onDataChange(p0: DataSnapshot) {
            logger.debug("onDataChange called with $p0.")
            val doorClosed = p0.getValue(Boolean::class.java)
            RestroomAvailabilityRepository.restroomAvailable = !doorClosed
        }
    }

    private val localAvailabilityChangeListener = object : RestroomAvailabilityRepository.OnRestroomAvailabilityChange {
        override fun onAvailabilityChange(isAvailable: Boolean) {
            logger.debug("onAvailabilityChange called with isAvailable=$isAvailable.")
            updateNotification(isAvailable)
        }
    }

    private fun updateNotification(available: Boolean) {
        NotificationCompat.Builder(this, "miSeMochka").apply {
            mContentTitle = getString(R.string.app_name)
            setSmallIcon(R.drawable.ic_notification)
            setOngoing(true)
            if (available) {
                mContentText = getString(R.string.the_restroom_is_available)
                color = ContextCompat.getColor(mContext, android.R.color.holo_green_dark)
                setColorized(true)
            } else {
                mContentText = getString(R.string.the_restroom_is_unavailable)
                color = ContextCompat.getColor(mContext, android.R.color.holo_red_dark)
            }
            setContentIntent(Intent(mContext, MainActivity::class.java).let {
                PendingIntent.getActivity(applicationContext, 0, it, PendingIntent.FLAG_UPDATE_CURRENT)
            })
        }.build().apply {
            if (available) {
                defaults = Notification.DEFAULT_ALL
                `when` = System.currentTimeMillis()
            }
            startForeground(R.id.availabilityNotification, this)
        }
    }

    override fun onDestroy() {
        restroomReference.removeEventListener(doorClosedStateListener)
        RestroomAvailabilityRepository.unsubscribe(localAvailabilityChangeListener)
        started = false
        super.onDestroy()
    }

    companion object : KLogging() {
        var started = false
            private set

        fun start(context: Context) {
            val intent = Intent(context, RestroomAvailabilityService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, RestroomAvailabilityService::class.java)
            context.stopService(intent)
        }
    }
}
