package de.ikas.iotrec.app.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import android.support.v4.content.ContextCompat.getSystemService
import de.ikas.iotrec.R

class NotificationHelper constructor(context: Context) {
    private var context = context

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = context.resources.getString(R.string.notification_channel_name_service)
            val descriptionText = context!!.resources.getString(R.string.notification_channel_description_service)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(context!!.resources.getString(R.string.notification_channel_id_service), name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = context!!.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }
}